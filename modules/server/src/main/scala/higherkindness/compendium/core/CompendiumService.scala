/*
 * Copyright 2018-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package higherkindness.compendium.core

import cats.effect.Sync
import cats.implicits._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.db.DBService
import higherkindness.compendium.models._
import higherkindness.compendium.models.parserModels.{ParserError, ParserResult}
import higherkindness.compendium.parser.ProtocolParserService
import higherkindness.compendium.storage.Storage

trait CompendiumService[F[_]] {

  def storeProtocol(id: ProtocolId, protocol: Protocol, idlName: IdlName): F[Unit]
  def recoverProtocol(protocolId: ProtocolId): F[Option[FullProtocol]]
  def existsProtocol(protocolId: ProtocolId): F[Boolean]
  def parseProtocol(protocolName: ProtocolId, target: IdlName): F[ParserResult]
}

object CompendiumService {

  implicit def impl[F[_]: Sync: Storage: DBService: ProtocolUtils: ProtocolParserService](): CompendiumService[
    F] =
    new CompendiumService[F] {

      override def storeProtocol(id: ProtocolId, protocol: Protocol, idlName: IdlName): F[Unit] =
        ProtocolUtils[F].validateProtocol(protocol) >>
          DBService[F].upsertProtocol(id, idlName) >>
          Storage[F].store(id, protocol)

      override def recoverProtocol(protocolId: ProtocolId): F[Option[FullProtocol]] =
        DBService[F]
          .selectProtocolMetadataById(protocolId)
          .flatMap {
            case Some(x) => Storage[F].recover(x)
            case _       => Sync[F].delay(None)
          }

      override def existsProtocol(protocolId: ProtocolId): F[Boolean] =
        DBService[F].existsProtocol(protocolId)

      override def parseProtocol(protocolId: ProtocolId, target: IdlName): F[ParserResult] =
        recoverProtocol(protocolId).flatMap {
          case Some(protocol) => ProtocolParserService[F].parse(protocol, target)
          case _ =>
            Sync[F].pure(
              ParserError(s"No Protocol Found with id: $protocolId").asLeft[FullProtocol])
        }
    }

  def apply[F[_]](implicit F: CompendiumService[F]): CompendiumService[F] = F
}

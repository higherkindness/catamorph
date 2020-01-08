/*
 * Copyright 2018-2020 47 Degrees, LLC. <http://www.47deg.com>
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
import higherkindness.compendium.core.refinements._
import higherkindness.compendium.metadata.MetadataStorage
import higherkindness.compendium.models._
import higherkindness.compendium.models.transformer.types.TransformResult
import higherkindness.compendium.storage.Storage
import higherkindness.compendium.transformer.ProtocolTransformer

trait CompendiumService[F[_]] {

  def storeProtocol(id: ProtocolId, protocol: Protocol, idlName: IdlName): F[ProtocolVersion]
  def retrieveProtocol(id: ProtocolId, version: Option[ProtocolVersion]): F[Option[FullProtocol]]
  def existsProtocol(id: ProtocolId): F[Boolean]
  def transformProtocol(fullProtocol: FullProtocol, target: IdlName): F[TransformResult]
}

object CompendiumService {

  implicit def impl[F[_]: Sync: Storage: MetadataStorage: ProtocolUtils: ProtocolTransformer]: CompendiumService[
    F] =
    new CompendiumService[F] {

      override def storeProtocol(
          id: ProtocolId,
          protocol: Protocol,
          idlName: IdlName): F[ProtocolVersion] =
        for {
          _       <- ProtocolUtils[F].validateProtocol(protocol)
          version <- MetadataStorage[F].store(id, idlName)
          _       <- Storage[F].store(id, version, protocol)
        } yield version

      override def retrieveProtocol(
          id: ProtocolId,
          version: Option[ProtocolVersion]): F[Option[FullProtocol]] =
        for {
          maybeMetadata <- MetadataStorage[F].retrieve(id)
          maybeProto <- maybeMetadata.flatTraverse(metadata =>
            Storage[F].retrieve(version.fold(metadata)(version =>
              metadata.copy(version = version))))
        } yield maybeProto

      override def existsProtocol(id: ProtocolId): F[Boolean] =
        MetadataStorage[F].exists(id)

      override def transformProtocol(
          fullProtocol: FullProtocol,
          target: IdlName): F[TransformResult] =
        ProtocolTransformer[F].transform(fullProtocol, target).flatTap {
          case Right(fullProtocol) =>
            storeProtocol(
              fullProtocol.metadata.id,
              fullProtocol.protocol,
              fullProtocol.metadata.idlName)
          case Left(err) => Sync[F].raiseError[ProtocolVersion](err)
        }
    }

  def apply[F[_]](implicit F: CompendiumService[F]): CompendiumService[F] = F
}

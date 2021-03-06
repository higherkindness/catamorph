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

package higherkindness.compendium.metadata.pg

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import higherkindness.compendium.core.doobie.implicits._
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.metadata.MetadataStorage
import higherkindness.compendium.models.{IdlName, ProtocolMetadata, ProtocolNotFound}

object PgMetadataStorage {

  def apply[F[_]: Async](xa: Transactor[F]): MetadataStorage[F] =
    new MetadataStorage[F] {

      override def store(id: ProtocolId, idlName: IdlName): F[ProtocolVersion] =
        Queries
          .store(id, idlName.entryName)
          .withUniqueGeneratedKeys[ProtocolVersion]("version")
          .transact(xa)

      override def retrieve(id: ProtocolId): F[ProtocolMetadata] =
        F.handleErrorWith(Queries.retrieve(id).unique.transact(xa)) { e =>
          F.raiseError(ProtocolNotFound(e.getMessage))
        }

      override def exists(id: ProtocolId): F[Boolean] =
        Queries.exists(id).unique.transact(xa)

      override def ping: F[Boolean] = Queries.checkConn.unique.transact(xa)
    }
}

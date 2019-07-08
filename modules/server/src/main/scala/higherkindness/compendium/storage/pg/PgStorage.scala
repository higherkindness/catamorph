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

package higherkindness.compendium.storage.pg

import cats.effect.Bracket
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.storage.Storage

private class PgStorage[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]) extends Storage[F] {

  override def store(id: ProtocolId, protocol: Protocol): F[Unit] =
    Queries.storeProtocol(id, protocol).run.void.transact(xa)
  override def recover(id: ProtocolId): F[Option[Protocol]] =
    Queries.recoverProtocol(id).option.transact(xa)
  override def exists(id: ProtocolId): F[Boolean] =
    Queries.protocolExists(id).unique.transact(xa)
}

object PgStorage {

  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): Storage[F] =
    new PgStorage(xa)

}

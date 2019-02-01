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

package higherkindness.compendium.db

import cats.effect.Sync
import cats.implicits._
import mouse.all._
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.models.ProtocolAlreadyExists
import higherkindness.compendium.storage.Storage

object DBServiceStorage {

  def impl[F[_]: Sync: Storage]: DBService[F] =
    new DBService[F] {

      override def addProtocol(id: String, protocol: Protocol): F[Unit] =
        for {
          exists <- Storage[F].checkIfExists(id)
          _ <- exists.fold(
            Sync[F].raiseError(new ProtocolAlreadyExists(s"Protocol with id ${id} already exists")),
            Storage[F].store(id, protocol))
        } yield ()
    }
}

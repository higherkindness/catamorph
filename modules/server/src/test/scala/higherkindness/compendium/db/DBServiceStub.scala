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

import cats.effect.IO
import higherkindness.compendium.models.{IdlName, ProtocolMetadata}
import higherkindness.compendium.core.refinements.ProtocolId

class DBServiceStub(val exists: Boolean, protocol: Option[ProtocolMetadata] = None)
    extends DBService[IO] {
  override def upsertProtocol(id: ProtocolId, idlNames: IdlName): IO[Unit] = IO.unit
  override def existsProtocol(id: ProtocolId): IO[Boolean]                 = IO.pure(exists)
  override def ping(): IO[Boolean]                                         = IO.pure(exists)

  override def selectProtocolMetadataById(id: ProtocolId): IO[Option[ProtocolMetadata]] =
    protocol.fold[IO[Option[ProtocolMetadata]]](IO.raiseError(new Throwable("Protocol not found")))(
      mp =>
        if (mp.id == id) IO(protocol)
        else IO.raiseError(new Throwable("Protocol not found")))
}

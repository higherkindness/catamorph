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

package higherkindness.compendium.storage

import cats.effect.IO
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.models._
import org.specs2.matcher.Matchers

class StorageStub(
    val proto: Option[Protocol],
    val identifier: ProtocolId,
    val protoVersion: ProtocolVersion
) extends Storage[IO]
    with Matchers {
  override def store(id: ProtocolId, version: ProtocolVersion, protocol: Protocol): IO[Unit] =
    IO {
      proto === Some(protocol)
      version === protoVersion
      id === identifier
    } *> IO.unit

  override def retrieve(metadata: ProtocolMetadata): IO[FullProtocol] =
    if (metadata.id == identifier && metadata.version == protoVersion)
      proto.fold(IO.raiseError[FullProtocol](ProtocolNotFound("Not Found")))(p =>
        IO.pure(FullProtocol(metadata, p))
      )
    else IO.raiseError[FullProtocol](ProtocolNotFound("Not found"))

  override def exists(id: ProtocolId): IO[Boolean] =
    IO(id == identifier)
}

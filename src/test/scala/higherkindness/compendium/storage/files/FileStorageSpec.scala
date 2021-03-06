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

package higherkindness.compendium.storage.files

import java.io.File
import java.nio.file.Paths

import cats.effect.IO
import higherkindness.compendium.CompendiumArbitrary._
import higherkindness.compendium.core.refinements.{ProtocolId, ProtocolVersion}
import higherkindness.compendium.models.config.FileStorageConfig
import higherkindness.compendium.models.{FullProtocol, Protocol, ProtocolMetadata}
import higherkindness.compendium.storage.{files, Storage}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterEach, BeforeAfterAll}

import scala.reflect.io.Directory

object FileStorageSpec extends Specification with ScalaCheck with BeforeAfterAll with AfterEach {

  private[this] lazy val basePath: String = "/tmp/filespec"
  private[this] lazy val storageConfig: FileStorageConfig = FileStorageConfig(
    Paths.get(s"$basePath")
  )
  private[this] lazy val baseDirectory    = new Directory(new File(basePath))
  private[this] lazy val storageDirectory = new Directory(new File(storageConfig.path.toUri))
  private[this] def storageProtocol(id: ProtocolId, version: ProtocolVersion) =
    new Directory(new File(s"$basePath${File.separator}${FileStorage.buildFilename(id, version)}"))

  private[this] lazy val fileStorage: Storage[IO] = files.FileStorage[IO](storageConfig)

  override def beforeAll(): Unit = {
    val _ = baseDirectory.createDirectory()
  }

  baseDirectory.createDirectory()
  override def afterAll(): Unit = {
    val _ = baseDirectory.deleteRecursively()
  }

  override def after = {
    baseDirectory.deleteRecursively()
    val _ = baseDirectory.createDirectory()
  }

  sequential

  "Store a file" >> {
    "Successfully stores a file" >> prop { (metadata: ProtocolMetadata, protocol: Protocol) =>
      val storageProtocolFile = storageProtocol(metadata.id, metadata.version)

      val io = fileStorage
        .store(metadata.id, metadata.version, protocol)
        .map(_ => storageDirectory.exists && storageProtocolFile.exists)

      io.unsafeRunSync() should beTrue
    }

    "Successfully stores and recovers a file" >> prop {
      (metadata: ProtocolMetadata, protocol: Protocol) =>
        val file = for {
          _ <- fileStorage.store(metadata.id, metadata.version, protocol)
          f <- fileStorage.retrieve(metadata)
        } yield f

        file.unsafeRunSync() must_=== FullProtocol(metadata, protocol)
    }

    "Returns true if there is a file" >> prop { (metadata: ProtocolMetadata, protocol: Protocol) =>
      val file = for {
        _      <- fileStorage.store(metadata.id, metadata.version, protocol)
        exists <- fileStorage.exists(metadata.id)
      } yield exists

      file.unsafeRunSync() should beTrue
    }

    "Returns false if there is no file" >> prop { protocolId: ProtocolId =>
      val out = for {
        exists <- fileStorage.exists(protocolId)
      } yield exists

      out.unsafeRunSync() should beFalse
    }

  }

}

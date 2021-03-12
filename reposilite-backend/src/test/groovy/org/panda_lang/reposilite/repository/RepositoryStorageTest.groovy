/*
 * Copyright (c) 2020 Ole Ludwig
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

package org.panda_lang.reposilite.repository

import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.panda_lang.reposilite.storage.FileSystemStorageProvider

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import static org.junit.jupiter.api.Assertions.assertEquals

@CompileStatic
class RepositoryStorageTest {

    @TempDir
    protected Path temp

    private RepositoryStorage repositoryStorage

    @BeforeEach
    void setUp() {
        repositoryStorage = new RepositoryStorage(temp, FileSystemStorageProvider.of(Paths.get(""), "10GB"))
    }

    @Test
    void 'should add size of written file to the disk quota'() {
        def initialUsage = repositoryStorage.storageProvider.getFileSize(Paths.get("")).get()
        def string = "test"
        def expectedUsage = initialUsage + string.bytes.length

        repositoryStorage.storeFile(new ByteArrayInputStream(string.bytes), temp)

        assertEquals expectedUsage, repositoryStorage.storageProvider.getFileSize(Paths.get("")).get()
    }
}

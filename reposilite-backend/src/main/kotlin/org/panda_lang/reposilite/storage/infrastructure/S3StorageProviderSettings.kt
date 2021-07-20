/*
 * Copyright (c) 2021 dzikoysk
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

package org.panda_lang.reposilite.storage.infrastructure

import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(name = "s3", description = ["Amazon S3 storage provider settings"])
internal class S3StorageProviderSettings : Runnable {

    @Parameters(index = "0", paramLabel = "<bucket-name>")
    lateinit var bucketName: String

    @Parameters(index = "1", paramLabel = "<region>")
    lateinit var region: String

    override fun run() { /* Validation */ }

}
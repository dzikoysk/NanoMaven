<!--
  - Copyright (c) 2021 dzikoysk
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div class="container mx-auto pt-10 px-6">
    <div v-for="entry in configurations" :key="entry.type">
      <h1 class="text-lg font-bold">{{entry.type}}</h1>
      <pre class="my-5 py-5 px-6 rounded-lg shadow-md bg-gray-50 dark:bg-gray-900">{{trim(entry.snippet)}}</pre>
    </div>
  </div>
</template>

<script>
const configurations = [
  {
    type: 'Maven',
    snippet: `
    <repository>
        <name>${window.REPOSILITE_TITLE}</name>
        <id>${window.REPOSILITE_ID}</id>
        <url>${window.location}</url>
    </repository>
    `
  },
  {
    type: 'Gradle Groovy',
    snippet: `
    maven {
        url "${window.location}"
    }
    `
  },
  {
    type: 'Gradle Kotlin',
    snippet: `
    maven {
        url = uri("${window.location}")
    }
    `
  },
  {
    type: 'Panda',
    snippet: `
    repositories: [
        ${window.location}
    ]
    `
  },
  {
    type: 'SBT',
    snippet: `
    resolvers += "${window.REPOSILITE_TITLE}" at "${window.location}"
    `
  }
]

export default {
  setup() {
    return {
      configurations
    }
  },
  methods: {
    trim(snippet) {
      const indentation = snippet.length - snippet.trimStart().length - 1
      
      return snippet.split('\n')
        .map(line => line.substring(indentation))
        .join('\n')
        .trim()
    }
  }
}
</script>

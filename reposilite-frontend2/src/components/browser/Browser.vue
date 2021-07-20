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
  <div class="bg-gray-100">
    <div class="bg-gray-100 dark:bg-black">
      <div class="container mx-auto">
        <p class="py-4 font-semibold">Index of / <span class="font-normal text-xl text-gray-500"> ⤴ </span></p>
      </div>
    </div>
    <hr class="border-gray-100 dark:border-gray-900">
    <div>
      <div class="container mx-auto relative">
        <div class="lg:absolute pt-5 -top-24.5 right-8">
          <Card/>
        </div>
        <div class="pt-2">
          <div v-for="file in files" v-bind:key="file" class="flex flex-row mb-1.5 py-3 rounded-full bg-white max-w-2/5 cursor-pointer">
            <div class="text-xm px-6 pt-1.75">⚫</div>
            <div class="font-semibold">{{file}}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Card from './Card.vue'

export default {
  components: {
    Card
  },
  data() {
    return {
      files: [ 'releases', 'snapshot' ],
    }
  }
}
</script>
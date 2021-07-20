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
  <div class="bg-gray-100 dark:bg-black">
    <div class="container mx-auto flex flex-row pt-17 pb-12">
      <div class="w-35">
        <img class="border-2 rounded-full dark:border-gray-700" src="https://avatars.githubusercontent.com/u/75123628?s=200&v=4">
      </div>
      <div class="flex flex-col justify-center px-10">
        <div>
          <p>Public Maven repository for Bookkity organization</p>
        </div>
        <div class="flex flex-row py-2">
          <GlobeIcon />
          <a class="px-3 text-gray-500" href="https://github.com/bookkity">https://github.com/bookkity</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import GlobeIcon from '../icons/GlobeIcon.vue'

export default {
  components: {
    GlobeIcon
  }
}
</script>
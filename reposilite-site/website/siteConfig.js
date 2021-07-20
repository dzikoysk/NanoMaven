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

const siteConfig = {
  title: 'Reposilite',
  tagline: 'Lightweight repository manager for Maven artifacts',
  url: 'https://reposilite.com',
  baseUrl: '/',
  copyright: `Copyright © ${new Date().getFullYear()} dzikoysk with ❤ panda-lang`,

  projectName: 'reposilite-site',
  organizationName: 'panda-lang',

  headerLinks: [
    { doc: 'about', label: 'Guide' },
    { page: 'help', label: 'Help' },
    { href: 'https://panda-lang.org/support', label: 'Support' },
    { href: 'https://github.com/dzikoysk/reposilite', label: 'GitHub' }
  ],

  headerIcon: '',
  footerIcon: 'img/favicon.png',
  favicon: 'img/favicon.png',

  colors: {
    primaryColor: '#101357',
    secondaryColor: '#fff'
  },

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks.
    theme: 'idea'
  },

  scripts: ['https://buttons.github.io/buttons.js'],
  onPageNav: 'separate',
  // docsSideNavCollapsible: true,
  cleanUrl: true,

  ogImage: 'img/undraw_online.svg',
  twitterImage: 'img/undraw_tweetstorm.svg',
  twitterUsername: 'dzikoysk'
}

module.exports = siteConfig

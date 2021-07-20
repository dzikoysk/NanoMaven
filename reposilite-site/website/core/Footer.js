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

const React = require('react');

class Footer extends React.Component {
  docUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    const docsUrl = this.props.config.docsUrl;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    return `${baseUrl}${docsPart}${doc}`;
  }

  pageUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    return baseUrl + (language ? `${language}/` : '') + doc;
  }

  render() {
    return (
      <footer className="nav-footer" id="footer">
        <section className="sitemap center">
          <div>
            <h5>Guide</h5>
            <a href={this.docUrl('about', this.props.language)}>
              Getting Started
            </a>
            <a href={this.docUrl('install', this.props.language)}>
              Installation
            </a>
            <a href={this.docUrl('indexing', this.props.language)}>
              Developer API
            </a>
          </div>
          <div>
            <h5>Community</h5>
            <a href="https://github.com/dzikoysk/reposilite/issues" target="_blank">
              Report Issue
            </a>
            <a href="https://github.com/dzikoysk/reposilite" target="_blank">Star Reposilite</a>
            <a href="https://twitter.com/dzikoysk" target="_blank">Follow @dzikoysk</a>
          </div>
        </section>

        <section className="copyright">{this.props.config.copyright}</section>
      </footer>
    );
  }
}

module.exports = Footer;

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

const React = require('react')

const CompLibrary = require('../../core/CompLibrary.js')

const Container = CompLibrary.Container
const GridBlock = CompLibrary.GridBlock

class HomeSplash extends React.Component {
  render () {
    const { siteConfig, language = '' } = this.props
    const { baseUrl, docsUrl } = siteConfig
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`
    const langPart = `${language ? `${language}/` : ''}`
    const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`

    const SplashContainer = props => (
      <div className='homeContainer'>
        <div className='homeSplashFade'>
          <div className='wrapper homeWrapper'>{props.children}</div>
        </div>
      </div>
    )

    const ProjectTitle = props => (
      <div>
        <h2 className='projectTitle' style={{ color: 'black', paddingTop: '60px' }}>
          {props.title}
        </h2>
        <small>{props.tagline} 📦</small>
      </div>
    )

    const PromoSection = props => (
      <div className='section promoSection'>
        <div className='promoRow'>
          <div className='pluginRowBlock'>{props.children}</div>
        </div>
      </div>
    )

    const Button = props => (
      <div className='pluginWrapper buttonWrapper'>
        <a
          className='button'
          style={{
            width: '250px',
            background: '#101357',
            'marginTop': '15px',
            'border': 'none',
            'borderRadius': '20px',
            'color': 'white',
            'marginLeft': '10px',
            'marginRight': '10px',
          }}
          href={props.href}
          target={props.target}
        >
          {props.children}
        </a>
      </div>
    )

    return (
      <SplashContainer>
        <div className='inner'>
          <ProjectTitle tagline={siteConfig.tagline} title={siteConfig.title} />
          <PromoSection>
            <Button href={docUrl('about')}>Get started</Button>
            <Button href={docUrl('install')}>Download Reposilite</Button>
          </PromoSection> 
        </div>
      </SplashContainer>
    )
  }
}

class Index extends React.Component {
  render () {
    const { config: siteConfig, language = '' } = this.props

    const Block = props => (
      <Container
        padding={['bottom', 'top']}
        id={props.id}
        style={props.style}>
        <GridBlock
          align='center'
          contents={props.children}
          style={props.style}
          layout={props.layout}
        />
      </Container>
    )

    const WhatIs = () => (
      <div id='whatis' layout='twoColumn' style={{
        display: 'flex',
        justifyContent: 'center',
        paddingTop: '20px',
        paddingBottom: '20px',
        flexWrap: 'wrap'
      }}>
        <div>
          <h2># What is Reposilite?</h2>
          <ul>
            <li>Repository manager for Maven artifacts</li>
            <li>Replaces managers like Nexus, Archiva or Artifactory</li>
            <li>Open source project</li>
          </ul>
        </div>
        <div>
          <h2># Why?</h2>
          <ul>
            <li>Reduce usage of your resources to even 8MB of RAM</li>
            <li>95%+ test coverage</li>
            <li>Easy to use</li>
          </ul>
        </div>
      </div>
    )

    const Features = () => (
      <Block layout='fourColumn'>
        {[
          {
            title: 'Authorization',
            content: 'Token based authorization for indexing and deploy'
          },
          {
            title: 'Docker 🐋',
            content: 'Available docker images [docker.com/reposilite](https://hub.docker.com/r/dzikoysk/reposilite)',
          },
          {
            title: 'Dashboard',
            content: 'Simple repository browser with admin panel'
          },
          {
            title: 'API',
            content: 'Provided REST API to communicate with repository',
          }
        ]}
      </Block>
    )

    return (
      <div style={{ color: 'black' }}>
        <HomeSplash siteConfig={siteConfig} language={language} />
        <div className='mainContainer'>
          <WhatIs />
          <Features />
        </div>
      </div>
    )
  }
}

module.exports = Index

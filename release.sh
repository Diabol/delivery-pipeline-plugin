#!/bin/bash
set -x
set -e
if [ $# -eq 2 ]
  then
    branch=$1
    tag=$2
  else
    echo "Incorrect number of arguments"
    echo "Usage: release.sh <branch> <tag>"
fi

if [[ -n "$tag" ]] && [[ $tag =~ ^release-.* ]]; then
  version=`echo $tag | sed -n 's/release-\(.\)/\1/p'`
  head=`git rev-parse HEAD`
  commit=`git rev-list -n 1 $tag`
  if [[ "$head" == "$commit" ]]; then
    # prepare the ssh key
    openssl aes-256-cbc -K $encrypted_308f5ca9ca59_key -iv $encrypted_308f5ca9ca59_iv -in travis_deploy_key.enc -out travis_deploy_key -d
    chmod 600 travis_deploy_key
    eval `ssh-agent -s`
    ssh-add travis_deploy_key

    # prepare the repo
    git config user.name "diabolbuilder"
    git config user.email "info@diabol.se"
    git config --global push.default simple
    remote=`git config remote.origin.url | sed -n 's/https:\/\/github.com\/\(.*\)/git@github.com:\1/p'`
    git remote remove origin
    git remote add origin $remote
    git fetch
    git checkout -qf $branch

    # do the release
    mvn --settings settings.xml --batch-mode -DreleaseVersion=$version -Darguments="-DskipTests=true" -DskipTests=true -DscmCommentPrefix="[maven-release-plugin][skip ci]" release:prepare release:perform
  else
    echo "Tag $tag does not point to $branch/head, aborting release"
    exit 1
  fi
fi

#!/bin/bash
set -x
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
  git checkout -qf $branch
  head=`git rev-parse HEAD`
  commit=`git rev-list -n 1 $tag`
  if [[ "$head" == "$commit" ]]; then
    mvn --settings settings.xml --batch-mode -DdryRun=true -DreleaseVersion=$version -Darguments="-DskipTests=true" -DskipTests=true release:prepare release:perform
  else
    echo "Tag $tag does not point to $branch/head, cannot release"
  fi
fi

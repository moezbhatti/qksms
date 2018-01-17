#!/bin/bash

DOCS=${1:-docs} # where the mkdocs project is
DOCS_DIR=${2:-docs} # mkdocs docs_dir
PROJECT=${3:-qksms} # what folder we're importing

( # in a subshell
cd "$DOCS/$DOCS_DIR"
echo "    - 'Java Doc':"
echo "        - '$PROJECT': '$DOCS_DIR/$PROJECT'"
for f in $(find $PROJECT -type f -name "*.md") ; do
    echo "    - '&nbsp;$f': '$f'"
done
) >> $DOCS/mkdocs.yml

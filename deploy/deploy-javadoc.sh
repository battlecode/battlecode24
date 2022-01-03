BUCKET_NAME="bc-game-storage"

# TODO finish this, once i can actually get javadocs to work.
# Here's scraps of untested code:

# cd ..
# # Assumes version as second arg to deploy script
# ./gradlew release_docs_zip -Prelease_version=$1 --stacktrace
# mv battlecode-javadoc-$1.zip javadoc.zip
# unzip -d javadoc javadoc.zip
# rm javadoc.zip
# mkdir frontend/public/javadoc
# mv javadoc frontend/public
# cd deploy

# TODO put this into a bucket, and configure properly, etc

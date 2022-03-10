# TODO enforce state of repo up-to-date
# TODO enforce `gradle.properties` -- update `release_version`.
# TODO enforce that this update is pushed to master
# TODO note that java is a prereq. 

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

read -p "IMPORTANT: Ensure that the docs are in the 'javadoc' folder. Press enter to proceed..."

gsutil -m rm gs://$BUCKET_NAME/javadocs/2022/**
# Upload
cd ../javadoc
gsutil -m cp -r ** gs://$BUCKET_NAME/javadocs/2022/
cd ../deploy
# Enforce cache policy
gsutil -m setmeta -h "Cache-Control:no-cache" -r gs://$BUCKET_NAME/javadocs/2022/**
# TODO public check
gsutil -m acl ch -u AllUsers:R -r gs://$BUCKET_NAME/javadocs/2022/**

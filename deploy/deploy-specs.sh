# Note: this script is not in the specs folder, on purpose --
# I don't want this script to get deployed too.

BUCKET_NAME="bc-game-storage"

echo 'Do you wish to deploy these specs to the public? Type public and press enter.'
echo 'IMPORTANT: NEVER DO THIS BEFORE THE GAME IS RELEASED'
read -p 'input: ' ispublic

# Handle bucket:
# Clean
gsutil -m rm gs://$BUCKET_NAME/specs/2022/**
# Upload
cd ../specs
gsutil -m cp -r ** gs://$BUCKET_NAME/specs/2022/
cd ../deploy
# Enforce cache policy
gsutil -m setmeta -h "Cache-Control:no-cache" -r gs://$BUCKET_NAME/specs/2022/**
# Make public?
if [ "$ispublic" == 'public' ]
then
    gsutil -m acl ch -u AllUsers:R -r gs://$BUCKET_NAME/specs/2022/**
fi

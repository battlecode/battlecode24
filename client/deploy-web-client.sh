# TODO move this to deploy folder
# TODO enforce state of repo up-to-date
# TODO year as arg
# TODO prereqs: npm, tsc, java?, flatc

BUCKET_NAME="bc-game-storage"
# TODO make gs://$BUCKET_NAME/clients/2022/ a var, and use it

echo "IMPORTANT: Make sure that gameVersion is updated in client/visualizer/src/config."
echo "If it is not, change it now. Make sure to push to the repo later too."
read -p "Press enter to proceed..."

cd ../schema
npm install
# Don't do npm run build; 
# this regenerates flatbuffers which is pretty annoying
# npm run build
# TODO the following replacement could be done automatically i think
# echo "IMPORTANT: "
# echo "NOW, change line 3 of schema/ts/battlecode_generated.ts to \`import { flatbuffers } from \"flatbuffers\"\`."
# echo "DON'T FORGET TO SAVE!"
# read -p "When done, press enter to proceed..."
cd ../client

cd playback
npm install
npm run build
cd ../visualizer
npm install
npm run prod
cd ..

gsutil -m rm gs://$BUCKET_NAME/clients/2022/**
gsutil -m cp -r visualizer/out gs://$BUCKET_NAME/clients/2022/
gsutil -m cp visualizer/visualizer.html gs://$BUCKET_NAME/clients/2022/
gsutil -m setmeta -h "Cache-Control:no-cache" -r gs://$BUCKET_NAME/clients/2022/**

echo 'Do you wish to deploy the client to the public? Type public and press enter.'
echo 'IMPORTANT: NEVER DO THIS BEFORE THE GAME IS RELEASED'
read -p 'input: ' ispublic
if [ "$ispublic" == 'public' ]
then
    gsutil -m acl ch -u AllUsers:R -r gs://$BUCKET_NAME/clients/2022/**
fi


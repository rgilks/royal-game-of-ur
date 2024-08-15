# Builds and deploys a new zip file, then updates the lambda function.
# This speeds up development since 'amplify push' can take a long time

# Usage: ./deploy-lambda-fn.sh <function-name> <env> <profile>

# e.g. ./deploy-lambda-fn.sh inviteUsers devcljst tre

cd ../amplify/backend/function/$1/src || exit
# yarn install
# yarn format
# yarn test
mkdir -p dist
cp package.json index.mjs handler.cljs dist
cd dist
yarn install --production
zip -r ../index.zip *
cd ..
aws lambda update-function-code --no-cli-pager --function-name $1-$2 --zip-file fileb://index.zip --profile $3
rm index.zip
rm -rf dist
# HOW TO RELEASE A JAVA GAME

1. Update version numbers in the following places:
  - `engine/src/main/battlecode/common/GameConstants.java`
  - `client/visualizer/src/config.ts`
  - Specs, both at the top and in the changelog.
2. Decide whether you want the release to be public.
  - Check the value of `IS_PUBLIC` in `.github/workflows/release.yml`.
3. Create a Release on GitHub.
  - The release tag-name will be the version number. For example: "1.0.3".
  - You should use the version number as the release title too.

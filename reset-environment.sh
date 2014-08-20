#Delete all untracked files and directories (-d), even ignored ones (-x).
git clean -x -d --force --quiet

#Revert all modified files.
git reset --hard

git pull --rebase

# rm -rf ~/.m2/repository/me/sneer

./gradlew cleanEclipse eclipse

if [ -d "../rockpaperscissors" ]; then
  cp -f android-api/bin/*.jar ../rockpaperscissors/libs/
fi
./gradlew install

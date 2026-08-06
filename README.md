# workflow

## running db from docker

docker compose up --build

## running backend

cd .\apps\backend
.\gradlew.bat bootRun - with h2
.\gradlew.bat bootRun --args='--spring.profiles.active=docker' - with docker

## test backend

cd .\apps\backend
.\gradlew.bat test

## running frontend

cd .\apps\frontend
npm run dev

## test frontend

todo

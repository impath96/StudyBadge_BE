#!/bin/bash
echo "> Health check 시작"
echo "> curl -s http://localhost:8080/health-check "

for RETRY_COUNT in {1..15}
do
  # RESPONSE body 데이터에 'UP'이라는 글자가 있다면 성공, 없다면 다시 체크
  RESPONSE=$(curl -s http://localhost:8080/actuator/health-check)
  UP_COUNT=$(echo $RESPONSE | grep 'OK' | wc -l)

  if [ $UP_COUNT -ge 1 ]
  then # $up_count >= 1 ("OK" 문자열이 있는지 검증)
      echo "> Health check 성공"
      break
  else
      echo "> Health check의 응답을 알 수 없거나 혹은 status가 OK이 아닙니다."
      echo "> Health check: ${RESPONSE}"
  fi

  if [ $RETRY_COUNT -eq 10 ]
  then
    echo "> Health check 실패. "
    exit 1
  fi

  echo "> Health check 연결 실패. 재시도..."
  sleep 10
done
exit 0
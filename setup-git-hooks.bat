@echo off
echo 🔧 Git Hook 설정을 시작합니다...

:: pre-commit hook 생성
(
echo #!/bin/sh
echo.
echo echo "🔍 ktlint 검사를 실행합니다..."
echo ./gradlew ktlintCheck
echo.
echo if [ $? -ne 0 ]; then
echo     echo "❌ ktlint 검사 실패! 자동 수정을 실행합니다..."
echo     ./gradlew ktlintFormat
echo     echo "✅ 코드가 자동 수정되었습니다. 변경사항을 추가하고 다시 커밋하세요."
echo     echo "다음 명령어를 실행하세요:"
echo     echo "  git add ."
echo     echo "  git commit -m \"your commit message\""
echo     exit 1
echo fi
echo.
echo echo "✅ ktlint 검사 통과!"
echo exit 0
) > .git\hooks\pre-commit

echo ✅ Git Hook 설정이 완료되었습니다!
echo 이제 커밋할 때마다 자동으로 ktlint 검사가 실행됩니다.
pause
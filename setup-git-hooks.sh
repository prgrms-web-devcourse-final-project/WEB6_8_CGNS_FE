#!/bin/bash

# 팀원용 Git Hook 자동 설정 스크립트
echo "🔧 Git Hook 설정을 시작합니다..."

# pre-commit hook 생성
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh

echo "🔍 ktlint 검사를 실행합니다..."
./gradlew ktlintCheck

if [ $? -ne 0 ]; then
    echo "❌ ktlint 검사 실패! 자동 수정을 실행합니다..."
    ./gradlew ktlintFormat
    echo "✅ 코드가 자동 수정되었습니다. 변경사항을 추가하고 다시 커밋하세요."
    echo "다음 명령어를 실행하세요:"
    echo "  git add ."
    echo "  git commit -m \"your commit message\""
    exit 1
fi

echo "✅ ktlint 검사 통과!"
exit 0
EOF

# 실행 권한 부여
chmod +x .git/hooks/pre-commit

echo "✅ Git Hook 설정이 완료되었습니다!"
echo "이제 커밋할 때마다 자동으로 ktlint 검사가 실행됩니다."
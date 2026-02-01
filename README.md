# ElastiCache & Spring Security Demo Application

이 문서는 로컬 환경에서 Docker 이미지를 빌드하고, GitHub Container Registry(GHCR)에 푸시한 뒤, EC2 서버에 배포하는 전체 과정을 설명합니다.

---

## 1. Docker 이미지 빌드 (Local)

GitHub 사용자 이름을 환경 변수로 설정하고, OS에 맞는 명령어로 Docker 이미지를 빌드합니다.

```sh
# GitHub 사용자 이름 설정 (본인의 ID로 변경)
export GH_USER=<github username>
# 예시: export GH_USER=kimjava911

# Windows 사용자 (일반 빌드)
docker build -t ghcr.io/${GH_USER}/elasticache:latest .

# Mac 사용자 (Apple Silicon 등 멀티 아키텍처 지원 빌드)
# linux/amd64와 linux/arm64 플랫폼을 모두 지원하도록 빌드합니다.
docker buildx build --platform linux/amd64,linux/arm64 -t elasticache:latest .

docker tag elasticache:latest ghcr.io/${GH_USER}/elasticache:latest
```

### 로컬 실행 테스트
```sh
# docker ps
# docker compose ls
docker compose up -d
# docker compose down && docker compose up -d
# http://localhost/
```

---

## 2. Docker 이미지 푸시 (Local)

GitHub Container Registry에 이미지를 업로드하기 위해 로그인하고 푸시합니다.
*   GitHub Settings > Developer settings > Personal access tokens에서 `write:packages` 권한이 있는 토큰(Classic)이 필요합니다.

```sh
# GitHub Personal Access Token (PAT) 설정
# https://github.com/settings/tokens/new 에서 토큰 생성
export CR_TOKEN=<pat token>

# GHCR 로그인 (비밀번호 대신 토큰 사용)
echo $CR_TOKEN | docker login ghcr.io -u $GH_USER --password-stdin

# 이미지 푸시
docker push ghcr.io/${GH_USER}/elasticache:latest
```

---

## 3. EC2 서버 접속 (Local → Server)

AWS EC2 인스턴스에 SSH로 접속합니다. `.pem` 키 파일이 있는 디렉토리에서 실행하세요.

```sh
# EC2 인스턴스의 퍼블릭 DNS 주소 설정
export PUBLIC_DNS=<public dns>

# 키 파일 권한 변경 (보안상 필수, 읽기 권한만 부여)
chmod 400 *.pem

# SSH 접속
ssh -i $(ls *.pem) ubuntu@${PUBLIC_DNS}
```

---

## 4. Docker 설치 및 설정 (Server)

서버에 접속한 후 Docker를 설치하고 권한을 설정합니다.

```sh
# 패키지 목록 업데이트
sudo apt update

# Docker 설치 스크립트 다운로드 및 실행
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 현재 사용자(ubuntu)를 docker 그룹에 추가 (sudo 없이 docker 명령어 사용 위함)
sudo usermod -aG docker $USER

# 그룹 변경 사항 적용을 위해 로그아웃 후 재접속 필요 (또는 exit 후 다시 ssh 접속)
exit
```

---

## 5. 애플리케이션 배포 (Server)

서버에서 GHCR에 로그인하고, 최신 이미지를 받아 애플리케이션을 실행합니다.

### 5.1 GHCR 로그인
```sh
# Docker 설치 확인
docker -v
docker compose version

# 환경 변수 재설정 (서버 세션이므로 다시 설정 필요)
export CR_TOKEN=<pat token>
export GH_USER=<github username>

# GHCR 로그인
echo $CR_TOKEN | docker login ghcr.io -u $GH_USER --password-stdin
```

### 5.2 프로젝트 설정 및 실행
```sh
git clone <github repo>
# password 요청 시 PAT
cd <repo name>
cp .env.sample .env
vi .env
```

```sh
docker pull ghcr.io/${GH_USER}/elasticache:latest
docker tag ghcr.io/${GH_USER}/elasticache:latest elasticache:latest
docker compose up -d
# docker compose down && docker compose up -d
```

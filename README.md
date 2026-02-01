# ElastiCache & Spring Security Demo Application

이 프로젝트는 Spring Boot, Spring Security(JWT), Redis(ElastiCache)를 활용한 인증/인가 데모 애플리케이션입니다.
로컬 개발 환경에서 Docker 이미지를 빌드하고, GitHub Container Registry(GHCR)를 통해 EC2 서버에 배포하는 전체 과정을 안내합니다.

---

## 1. Docker 이미지 빌드 (Local)

로컬 환경에서 애플리케이션의 Docker 이미지를 빌드합니다. 멀티 아키텍처(AMD64/ARM64)를 지원하도록 빌드하여 다양한 환경 호환성을 확보합니다.

```bash
# GitHub 사용자 이름 설정 (본인의 GitHub ID로 변경)
export GH_USER=<github_username>
# 예시: export GH_USER=kimjava911

# 이미지 빌드 (Mac Apple Silicon 사용자는 platform 옵션 권장)
# linux/amd64와 linux/arm64 아키텍처를 모두 지원하도록 빌드
docker buildx build --platform linux/amd64,linux/arm64 -t elasticache:latest .

# GHCR 업로드를 위한 태그 설정
docker tag elasticache:latest ghcr.io/${GH_USER}/elasticache:latest
```

### 로컬 실행 테스트
```bash
# 컨테이너 실행 (백그라운드)
docker compose up -d

# 실행 상태 확인
docker compose ls
docker ps

# 서비스 접속: http://localhost/
```

---

## 2. Docker 이미지 푸시 (Local)

빌드된 이미지를 GitHub Container Registry(GHCR)에 업로드합니다.
> **사전 준비**: GitHub Settings > Developer settings > Personal access tokens에서 `write:packages` 권한이 포함된 토큰(Classic)을 생성해야 합니다.

```bash
# GitHub Personal Access Token (PAT) 환경변수 설정
export CR_TOKEN=<your_pat_token>

# GHCR 로그인
echo $CR_TOKEN | docker login ghcr.io -u $GH_USER --password-stdin

# 이미지 푸시
docker push ghcr.io/${GH_USER}/elasticache:latest
```

---

## 3. EC2 서버 접속 (Local → Server)

AWS EC2 인스턴스에 SSH로 접속하여 배포 환경을 구성합니다.

```bash
# EC2 인스턴스 퍼블릭 DNS 주소 설정
export PUBLIC_DNS=<ec2_public_dns>

# 키 파일 권한 설정 (보안을 위해 읽기 권한만 부여)
chmod 400 *.pem

# SSH 접속
ssh -i $(ls *.pem | head -n 1) ubuntu@${PUBLIC_DNS}
```

---

## 4. Docker 설치 및 설정 (Server)

서버 환경에 Docker를 설치하고, `ubuntu` 사용자가 `sudo` 없이 Docker 명령어를 사용할 수 있도록 권한을 설정합니다.

```bash
# 패키지 목록 업데이트
sudo apt update

# Docker 설치 스크립트 실행
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 그룹 권한 적용을 위해 세션 재접속 (로그아웃 후 다시 SSH 접속)
exit
```

---

## 5. 애플리케이션 배포 (Server)

서버에서 GHCR에 로그인하고, 최신 이미지를 받아 애플리케이션을 실행합니다.

### 5.1 GHCR 로그인
```bash
# Docker 설치 확인
docker -v
docker compose version

# 환경 변수 설정 (서버 세션에서 다시 설정)
export CR_TOKEN=<your_pat_token>
export GH_USER=<github_username>

# GHCR 로그인
echo $CR_TOKEN | docker login ghcr.io -u $GH_USER --password-stdin
```

### 5.2 프로젝트 설정 및 실행
```bash
# 프로젝트 클론 (비밀번호 입력 시 PAT 사용)
git clone <github_repo_url>
cd <repo_name>

# 환경 변수 파일 설정
cp .env.sample .env
vi .env  # 필요한 환경 변수(APP_NAME 등) 수정

# 최신 이미지 풀링 및 태그 설정
docker pull ghcr.io/${GH_USER}/elasticache:latest
docker tag ghcr.io/${GH_USER}/elasticache:latest elasticache:latest

# 서비스 실행
docker compose up -d

# (선택) 서비스 재시작
# docker compose down && docker compose up -d
```

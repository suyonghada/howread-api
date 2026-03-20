# AWS 인프라 구성 체크리스트

리전: ap-northeast-2 (서울) 고정

## 구성 순서

- [x] 01. VPC 및 네트워크
- [x] 02. Security Group
- [x] 03. RDS MySQL
- [x] 04. ECR 리포지토리
- [x] 05. IAM — EC2 Role
- [x] 06. IAM — GitHub Actions OIDC Role
- [x] 07. EC2 인스턴스 + Elastic IP
- [ ] 08. Route 53 도메인 구매 + ACM 인증서 (도메인: howread.org)
- [x] 09. GitHub Secrets 등록
- [x] 10. EC2 초기 설정 (Docker, .env, Flyway)
- [x] 11. 첫 배포 트리거 및 검증
- [ ] 12. HTTPS 설정 (Let's Encrypt + nginx.conf 배포)

## 아키텍처 요약

```
Internet
  │
  ▼
Route 53 (api.도메인.com → Elastic IP)
  │
  ▼
EC2 — Nginx (80/443)
       └── Spring Boot App (8080, Docker)
              │
              ▼
           RDS MySQL (private subnet)
```

## 생성된 리소스 메모

| 리소스 | 이름 / ID | 비고 |
|---|---|---|
| VPC | howread-vpc | |
| Public Subnet 1 | | ap-northeast-2a |
| Public Subnet 2 | | ap-northeast-2c |
| Private Subnet 1 | | ap-northeast-2a (RDS용) |
| Private Subnet 2 | | ap-northeast-2c (RDS용) |
| SG (EC2) | howread-sg-ec2 | |
| SG (RDS) | howread-sg-rds | |
| RDS Endpoint | | |
| ECR URI | | |
| EC2 Instance ID | | |
| Elastic IP | 3.38.53.29 | |
| Domain | howread.org | Route 53 구매 예정 |
| API Domain | api.howread.org | |
| IAM Role (EC2) | howread-ec2-role | |
| IAM Role (GitHub) | howread-github-actions-role | ARN: |
| ACM 인증서 ARN | | 발급 후 기록 |

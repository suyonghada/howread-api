# 07. Route 53 + ACM 인증서

## Route 53 도메인 구매 및 Hosted Zone

Route 53에서 직접 구매 → Hosted Zone 자동 생성:

1. Route 53 → **Register domains** → `howread.org` 검색 후 구매
2. 구매 완료 시 Hosted Zone이 **자동 생성**됨 (별도 생성 불필요)
3. DNS 전파까지 최대 48시간 소요 (보통 수십 분)

> 외부 등록업체(가비아 등)에서 구매한 경우:
> 1. Route 53 → Hosted zones → **Create hosted zone** → `howread.org` (Public)
> 2. 생성된 **NS 레코드 4개**를 등록업체 네임서버에 입력

## ACM SSL 인증서

> ⚠️ 반드시 **ap-northeast-2 (서울)** 리전에서 발급

1. Certificate Manager → **Request a certificate**
2. **Request a public certificate** → Next
3. Domain names:
   - `howread.org`
   - `*.howread.org` (Add another name)
4. Validation method: **DNS validation**
5. **Request**
6. 인증서 선택 → **Create records in Route 53** (자동 CNAME 추가)
7. Status가 **Issued**로 바뀔 때까지 대기 (5~30분)

## A 레코드 생성

1. Route 53 → Hosted zones → `howread.org`
2. **Create record**

| 항목 | 값 |
|---|---|
| Record name | `api` |
| Record type | A |
| Value | `3.38.53.29` (EC2 Elastic IP) |
| TTL | 300 |

3. **Create records**

## 완료 후 기록

- 도메인: `howread.org`
- API 도메인: `api.howread.org`
- EC2 Elastic IP: `3.38.53.29`
- ACM 인증서 ARN: `arn:aws:acm:ap-northeast-2:...` ← 발급 후 기록

## 검증

```bash
# DNS 전파 확인
nslookup api.howread.org
# → 3.38.53.29 반환되면 진행

# ACM 상태
# AWS 콘솔에서 Status: Issued 확인
```

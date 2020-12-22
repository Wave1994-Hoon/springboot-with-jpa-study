#### 단축키
- command + shift + f: 파일내에서 찾기
- option + enter: static import(add)

#### FK
- 데이터 정합성이 중요하면 사용 ex) 결제(돈)
- 실시간 트래픽이 많고 유연하개 운영하고 싶다면 인덱스로 조회해도 

#### Getter & Setter
- Getter: 모두 열어두는 것이 편리함 -> 단순히 호출만 하기 때문에
- Setter: 엔티티 데이터가 어떻게 왜 변경되었는지 추적하기가 어려움
    - Setter 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야함 
    
#### 애플리케이션 아키텍처
- controller, web: 웹 계층
- service: 비즈니스 로직, 트랜잭션 처
- repository: JPA를 직접 사용하는 계층, 엔티티 매니저 사용
- domain: 엔티티가 모여 있는 계층, 모든 계층에서 사용

#### Injection
- 필드 주입
  - @Autowired </br>
    MemberRepository memberRepository
- 생성자 주입
    - private final MemberRepository memberRepository </br>
      public MemberService(MemberRepository memberRepositroy) { this.memberRepositroy = memberRepository }
      
    - 생성자 주입을 권징
    - 변경 불가능한 안전한 객체 생성 가능
    - 생성자가 하나면, @Autowired를 생략할 수 있다.
    - final 키워드를 추가하면 컴파일 시점에 memberRepositroy를 설정하지 않는 오류를 체크할 수 있다. (보통 기본 생성자를 추가할 때 발견)
    
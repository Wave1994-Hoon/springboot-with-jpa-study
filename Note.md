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
    

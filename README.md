# Entrepreneurship-Capstone-Design-1-Team-Marshmallow

외국인 여행객들을 위한 AR 보행자 네비게이션


## 🔍 시연 영상

![demo1](https://github.com/Baek2back/Entrepreneurship-Capstone-Design-1-Team-Marshmallow/raw/master/Demo.gif)
![demo2](https://github.com/Baek2back/Entrepreneurship-Capstone-Design-1-Team-Marshmallow/raw/master/Demo1.gif)



## 🌟 목적

* 전세계적으로 사용되는 Google Maps의 경우 한국에서는 사용이 제한적이므로 오픈 소스 기반의 지도를 활용하여 국내에서도 외국인들이 쉽게 경로를 제공받을 수 있도록 한다. 

* 도쿄의 한 지하철 역으로부터 1km 가량 떨어져 있는 곳에 위치한 '선샤인 아쿠아리움'에서 누구나 좋아하는 수족관의 인기 캐릭터인 '펭귄'을 수족관까지 안내하는 길잡이로써의 역할을 하게 하여 성공적으로 방문객 수를 늘릴 수 있었던 사례를 벤치마킹 하였다.

  ![image1](https://github.com/Baek2back/Entrepreneurship-Capstone-Design-1-Team-Marshmallow/raw/master/images/image1.jpg)

* 현재 충무로역 인근의 도보는 정돈되어 있지 않고 골목이 많아 도보 길찾기가 굉장히 제한되므로 보행자 네비게이션을 개발한다.

* 명동, 남산타워 등 인근 관광 명소에 방문한 외국인들이 서애로로 유입될 수 있도록 관광 안내 어플리케이션으로 개발한다.

## ⚙️ 세부 구현

1. 역사, 영화 테마 별로 목적지를 설정해두고, 사용자가 희망하는 장소를 선택할 수 있도록 한다.
2. Mapbox Navigation API를 이용하여 현재 위치와 설정된 목적지 간의 경로를 그린다.
3. OpenGL을 이용하여 3D 캐릭터를 화면 위(정중앙)에 띄우고, 현재 기기에 수신되는 방위각(Azimuth)만큼 캐릭터를 지속적으로 회전시킨다.
4. 사용자가 진행 중인 방향을 직관적으로 확인하고, 경로에 맞게 진행 방향을 수정하여 목적지에 제대로 도착할 수 있게끔 유도한다. 

## 🔨 Built With

- Android Studio with Java
- Mapbox API
- OpenGL

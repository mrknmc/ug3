---
title: SDP Final Individual Report
author: Mark Nemec - s1140740
csl: ieee.csl
fontsize: 12pt
---

# Introduction

The purpose of this report is to describe my contribution to the SDP project for team 2, The Underdogs, of which I was the leader. My contribution to the team ranged from managing the team to working on all parts of the code base and robot design.

# Team Responsibilities

When it comes to team responsibilities, I was active from the beginning. I set up version control on GitHub [[@repo]](#appendix), task-management on Trello and a discussion group on Facebook. Moreover, I wrote a guide for setting up the project on DiCE [[@setup]](#appendix).

I attended vast majority of the team meetings. Throughout the semester, I tried organising meetings regularly even though the turnout was usually not great. I created tasks on Trello and issues on GitHub for other team members so that they would know what parts of the project to work on.

I tried to get everyone involved and informed other team members whenever I was working on the project. I was always willing to help, e.g. after we had a conflict at the last performance review our mentor and I had a meeting with one of the team members who stopped showing up to talk it through with him.

# Vision

I started working on the vision system in week 2. I was suggesting to use a vision library because our implementation at the time was needlessly complicated and slow - running at about 10 frames per second (fps). I also implemented retrieving the orientation of the robots from the vision system before milestone 3.

After our failure to field well-performing robots for milestone 3 and the assessed friendlies the team decided to change the vision system to use OpenCV library [[@opencv]](#appendix). The core of the new system was implemented by me with assistance from Julien in a span of a week. The system was based on the vision system of group 9 from 2012 [[@group9]](#appendix) who also used OpenCV.

The new vision system correctly detected the position of the ball, and the position and orientation of robots. It ran at optimal 25 fps and was much simpler to parametrise. Moreover, I worked on including camera lens distortion correction and perspective correction. These improvements allowed for a more precise representation of objects on the pitch. For example after the perspective correction, the x-coordinate of the ball was within 1 cm from the x-coordinate of the robot when they were aligned on the pitch.

Additionally, I worked on the main GUI of our program. For example, I re-wrote the thresholding tools so we would get immediate feedback after we change thresholds and I helped Gordon implement saving thresholds for each PC in a separate JSON file. This greatly reduced the time required for thresholding.

# Object Representation and Geometry

I wrote object representation and geometry classes in week 3. These were incrementally updated, as needed. After the assessed friendlies, I had added unit tests for object representation and geometry so we could be sure that our code is not broken at a fundamental level when we started working on strategy.

I also kept on adding library-like methods to the `MovableObject` and `Robot` classes which were fundamental to our strategy system.

# Strategy

I started working on strategy only after the assessed friendlies as I was focusing on other parts of the project before that. At that point we only had a working defender shot-blocking strategy. Thus, I added strategy for passing the ball up to the attacker and trying to score with the attacker.

I also refactored the defender strategy to use the previously mentioned library-like methods from object representation. This provided better transparency and reusability of the strategy code.

# Robot Design

After multiple unsuccessful designs we decided that we would 'borrow' robot design ideas from other more successful teams. Julien and I therefore created a new chassis and grabber/kicker for the robots. This design was later expanded upon by other members of the team into what ended up being our final robot design.

With the new grabber design the robot was able to reliably grab a ball from a distance of 13 cm from the centre of the robot. Moreover, the kicker was able to kick the ball with good speed - it was easily able to score across the whole pitch.

# Conclusion

In conclusion, I believe that as the team leader and most active contributor to the project [[@contribs]](#appendix), my contribution was extensive and valuable. Although our team did not do remarkably well, I feel that I improved not only my programming skills, but also my ability to work as part of a team and my ability to communicate my thoughts.

\newpage

# Appendix

---
references:
- id: repo
  title: Group 2 SDP GitHub Repository
  type: webpage
  URL: 'http://github.com/sdpgroup2/sdp/'
- id: setup
  title: DiCE Set-up README
  type: webpage
  URL: 'https://github.com/sdpgroup2/sdp/blob/master/documentation/howto/README_DICE_SETUP.md'
- id: opencv
  title: OpenCV
  type: webpage
  URL: 'http://opencv.org'
- id: tasks
  title: Tasks
  type: webpage
  URL: 'https://github.com/sdpgroup2/sdp/blob/master/pc/tasks.md'
- id: group9
  title: Group 9 (2012) Google Code Repository
  type: webpage
  URL: 'https://code.google.com/p/group-9-sdp2012/'
- id: contribs
  title: Project Contributions
  type: webpage
  URL: 'https://github.com/sdpgroup2/sdp/graphs/contributors'
---

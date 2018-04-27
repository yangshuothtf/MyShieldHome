# MyShieldHome

This is a phone call and location monitor APP, based on Android 5.1--6.0. <br>
Already tested on LeMax X900 and 360 OS<br>

Feature list:<br>
1. Monitor incoming/outgoing calls.<br>
   1.1 Save every call number even if more than 2 calls at same time.<br>
   1.2 Save missing call number.<br>
2. Report call record via email whenever network available.<br>
   2.1 maintain a list for all calls recorded, send out when network available, and then delete files on list.<br>
-- above done by 2018.1.26<br>
3. Send/receive command via email. -- (done, 2018.2.8)<br>
   3.1 Check command email when first call after reboot -- done, 2018.4.3<br>
   3.2 If GPS location enabled, ignore the flag for first phone call check, and check command email when GPS info reported.<br>
   3.3 Set parameters via email for device name, monitor type, GPS location interval, GPS report time etc. <br>
4. Keep mailbox clean<br>
   4.1 Delete phone call email after sent -- (done,2018.2.9)<br>
   4.2 Delete GPS info email except the latest one.<br>
   4.3 Keep only one latest GPS info mail for each device.<br>
5. Power saving, no GPS enable needed.<br>
   5.1 GPS locate trigger by alarmManager, every 10 minutes --done, 2018.4.12<br>
   5.2 GPS locate when diff more than 300 meters.<br>
To do list:<br>
1. Speech to text for call recorded.<br>
2. Accessibility for weChat<br>
3. Auto-update in background<br>
4. More config options like mailbox via command<br>
5. Blacklist for fraud calls.<br>
6. Bluetooth<br>
7. Tensorflow<br>

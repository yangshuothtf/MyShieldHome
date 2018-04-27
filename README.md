# MyShieldHome
Feature list:
1. Monitor incoming/outgoing calls.
   1.1 Save every call number even if more than 2 calls at same time.
   1.2 Save missing call number.

2. Report call record via email whenever network available.
   2.1 maintain a list for all calls recorded, send out when network available, and then delete files on list.
-- above done by 2018.1.26

3. Send/receive command via email. -- (done, 2018.2.8)
   3.1 Check command email when first call after reboot -- done, 2018.4.3
   3.2 If GPS location enabled, ignore the flag for first phone call check, and check command email when GPS info reported.
   3.3 Set parameters via email for device name, monitor type, GPS location interval, GPS report time etc. 

4. Keep mailbox clean
   4.1 Delete phone call email after sent -- (done,2018.2.9)
   4.2 Delete GPS info email except the latest one.
   4.3 Keep only one latest GPS info mail for each device.

5. Power saving, no GPS enable needed.
   5.1 GPS locate trigger by alarmManager, every 10 minutes --done, 2018.4.12
   5.2 GPS locate when diff more than 300 meters.
 
To do list:
1. Speech to text for call recorded.
2. Accessibility for weChat
3. Auto-update in background
4. More config options like mailbox via command
5. Blacklist for fraud calls.
6. Bluetooth
7. Tensorflow

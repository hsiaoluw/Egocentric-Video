# Egocentric-Video
For detail of the implementation please power point Egocentric-Video Summary.

Student Name: Wang, Yi-Wen  Student ID: 9221585532

Student Name: Wang, Hsiao-Lun  Student ID: 3517116028

Student Name: Yeh, Chien-Wei  Student ID:3838015178



compile at the root folder (folder same as readme)

Simple player: in folder AVplayer
compile and execut:
	javac AVplayer/*.java
	java AVplayer.AVplayer [Input_filename.rgb] [Input_filename.wav]

Summarization part:
compile and execut:
	javac Summary/*.java
	java  Summary.Summary [Input_filename.rgb] [Input_filename.wav]
It will generate two file summary_video.rgb and summary.wav. That will be opened by player.
 
FrameIndex part:
compile and execut:
	javac FrameInd/*.java
	java FrameInd.FrameInd [search video rgb] [search video wav] [query image.rgb] or java [search video rgb] [search video wav] [query image.rgb] [query image width] [query image height]
It will generate two file query_video.rgb and query_.wav. That could be opened by player.

Correction part:
compile and execut:
	javac Correction/*.java
	java Correction.Correction [Input_filename.rgb] [Input_filename.wav]

It will generate two file correction_video.rgb,  That could be opened by player.

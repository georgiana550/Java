1. 用crawler抓網站, 並把蒐集的網站資料放置在crawl目錄下。
2. run ProcessDocument class以建立 inverted index. (key word -> Document Id)
   (1) ProcessDocument class 會產生Doc_list.txt，這個檔案記錄著Document Id -> Document address的mapping
   (2) HTMLParser class及HTMLHandler class會對原始網頁做parsing，並在 parsed 目錄下產生parsed_file，parsed_file是符合CKIP(中文斷詞系統)格式的xml檔案。
   (3) Segmentation class會把parsed目錄下的xml檔一一丟給斷詞系統做斷詞，斷詞系統回傳的結果，會以xml檔案的格式儲存在 seg 目錄下。
   (4) Indexing class 負責將 seg 目錄下的斷詞結果，寫入InvertedIndex.txt。 
	/* 但由於斷詞系統回傳的xml格式是以big5編碼，所以Indexing class 會先呼叫 Big5toUTF8 class 將big5轉換為utf8編碼。*/
3. 有了Doc_list.txt 及 InvertedIndex.txt便可供UI做query，找出key word -> Document address的mapping.
4. UI 提供簡易的AND/OR/NOT boolean operation.

issues:
1.不知道是不是斷詞系統有改, 我在vista的電腦上run, 不做Big5toUTF8的動作才能正確產生segmentedFile.
2.在傳送parsedfile給斷詞系統;或接收斷詞系統回傳的segmentedFile時, 要注意byte buffer的大小, 否則傳送與接收有可能不完整.
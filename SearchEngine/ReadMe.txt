1. ��crawler�����, �ç�`����������Ʃ�m�bcrawl�ؿ��U�C
2. run ProcessDocument class�H�إ� inverted index. (key word -> Document Id)
   (1) ProcessDocument class �|����Doc_list.txt�A�o���ɮװO����Document Id -> Document address��mapping
   (2) HTMLParser class��HTMLHandler class�|���l������parsing�A�æb parsed �ؿ��U����parsed_file�Aparsed_file�O�ŦXCKIP(�����_���t��)�榡��xml�ɮסC
   (3) Segmentation class�|��parsed�ؿ��U��xml�ɤ@�@�ᵹ�_���t�ΰ��_���A�_���t�Φ^�Ǫ����G�A�|�Hxml�ɮת��榡�x�s�b seg �ؿ��U�C
   (4) Indexing class �t�d�N seg �ؿ��U���_�����G�A�g�JInvertedIndex.txt�C 
	/* ���ѩ��_���t�Φ^�Ǫ�xml�榡�O�Hbig5�s�X�A�ҥHIndexing class �|���I�s Big5toUTF8 class �Nbig5�ഫ��utf8�s�X�C*/
3. ���FDoc_list.txt �� InvertedIndex.txt�K�i��UI��query�A��Xkey word -> Document address��mapping.
4. UI ����²����AND/OR/NOT boolean operation.

issues:
1.�����D�O���O�_���t�Φ���, �ڦbvista���q���Wrun, ����Big5toUTF8���ʧ@�~�ॿ�T����segmentedFile.
2.�b�ǰeparsedfile���_���t��;�α����_���t�Φ^�Ǫ�segmentedFile��, �n�`�Nbyte buffer���j�p, �_�h�ǰe�P�������i�ण����.
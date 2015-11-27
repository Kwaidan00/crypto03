/*
 * Autor: Aleksander Spyra
 *
 * Program szyfrujący wskazany plik na dysku.
 * Kompilacja:
 * g++ -std=c++11 zad1.cpp -lcrypto
 *
 * Parametry uruchomienia:
 * argv[1] - schemat szyfrowania (dostępne: AES)
 * argv[2] - tryb szyfrowania (dostępne: CTR, CBC, GCM)
 * argv[3] - ścieżka do pliku keystore (własna implementacja my_keystore_main)
 * argv[4] - identyfikator klucza (ustawiony w keystore)
 * argv[5] - ścieżka do pliku, który ma zostać zaszyfrowany/odszyfrowany
 * argv[6] - typ operacji (decode/encode)
 *
 * ./a.out AES CBC ./keystore_file klucz old encode
 * ./a.out AES CBC ./keystore_file klucz enc decode
*/
#include <cstdio>
#include <cstdlib> //exit, atof
#include <string>
#include <map>
#include <iostream>
#include "my_crypto.h"
#include "fun.h"
#include "my_keystore_f.h"

using namespace std;

int main(int argc, const char* argv[])
{

//------------SPRAWDZANIE ILOSCI ARGUMENTOW-------------
	if (argc < 7)
	{
		printf("Zbyt mala liczba argumentow.\n");
		exit(-1);
	}
//---------------------------------------
	string encoding(argv[1]);
	if (encoding.compare("AES") == 0 )
	{
		
	}
	else
	{
		printf("Nieobslugiwany schemat szyfrowania\n");
		exit(-1);
	}
	string encoding_mode(argv[2]);
	if (encoding_mode.compare("CTR") == 0 || encoding_mode.compare("GCM") == 0 || encoding_mode.compare("CBC") == 0 )
	{
	}
	else
	{
		printf("Nieobslugiwany tryb szyfrowania\n");
		exit(-1);
	}
	string path_to_keystore(argv[3]);
	string key_id(argv[4]);
	string path_to_file(argv[5]);
	string mode(argv[6]);
	if (mode.compare("encode") == 0 || mode.compare("decode") == 0 )
	{
	}
	else
	{
		printf("Nieznany tryb pracy\n");
		exit(-1);
	}

	FILE *input, *output;
	if ( (input = fopen(argv[5], "rb")) == NULL)
	{
		printf("fopen error.");
		exit(-1);
	}
	if ( mode.compare("encode") == 0 )
	{
		output = fopen("enc", "wb");
	}
	else
	{
		output = fopen("dec", "wb");
	}

	unsigned char returned_key[128];
	int returned_key_len = get_decrypted_key("aleks", key_id, path_to_keystore, returned_key);
	unsigned char key[returned_key_len];

	for (int i = 0; i < returned_key_len; i++)
	{
		key[i] = returned_key[i];
	}
	if (returned_key_len != -1)
	{

		crypt_file(key, mode, encoding_mode, input, output);
	}


	fclose(input);
	fclose(output);
	exit(0);
}

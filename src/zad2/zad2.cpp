/* CTR - pozwala odszyfrować fragment danych
 * 
 * Kompilacja:
 * g++ -std=c++11 zad2.cpp -lSDL -lSDL_mixer -lcrypto
 */

#include <cstdio>
#include <cstdlib> //exit, atof
#include <string>
#include <map>
#include <iostream>
#include <SDL/SDL.h>
#include <SDL/SDL_mixer.h>
//#include "zad2_f.h"
#include "../zad1/fun.h"
#include "../zad1/my_crypto.h"
#include "../zad1/my_keystore_f.h"

using namespace std;

Mix_Music *music = NULL;

void clean()
{
	Mix_FreeMusic(music);
	Mix_CloseAudio();
	Mix_Quit();
}

int main(int argc, const char* argv[])
{
	string music_file(argv[1]);
	string keystore_path = "";
	string key_id = "";
	string password = "";
	char c;
	const char* key0_p = "ai5fngir3e01[fr0lkeq]=-12cvinfgf";
	const char* iv0_p = "amv[]0jdfie8r10K";
	unsigned char key0[32];
	unsigned char iv0[16];
	for (int i = 0; i < 32; i++)
	{
		key0[i] = key0_p[i];
	}
	for (int i = 0; i < 16; i++)
	{
		iv0[i] = iv0_p[i];
	}

	ifstream config_file_i0(".config0");

	if (config_file_i0.is_open() == false)
	{
		config_file_i0.close();
		printf("Creating file .config...\n");

		//FILE *config_file_o = fopen(".config", "wb");
		//FILE *config0_file_o = fopen(".config0", "wb");

		//ofstream config_file_o(".config");		// z zaszyfrowanym kluczem
		ofstream config0_file_o(".config0");		// z hashem

		string pin = "";
		printf("Set PIN:\n");
		set_stdin_echo(false);
		c = (char) getchar();
		while ( c != 10 )
		{
			pin.push_back(c);
			c = (char) getchar();
		}
		set_stdin_echo(true);
		//cout << "\nWpisales haslo: " << pin << "\n";
/*
 * Hashowanie hasla - idzie do pliku config0. Hashowanie hasla jest dobrym rozwiązaniem.
 */
		const char *pin_const_char = pin.c_str();
		unsigned char pin_uchar[pin.length()];
		for (int i = 0; i < pin.length(); i++)
		{
			pin_uchar[i] = pin_const_char[i];
		}	
		unsigned char pin_hash[EVP_MAX_MD_SIZE];
		unsigned int pin_hash_len = hash_sha512(pin_uchar, pin_hash, strlen(pin_const_char));
		string hashed_pin = hex_table(pin_hash, pin_hash_len);
		config0_file_o << hashed_pin << endl;
		//cout << "hash: " << hashed_password << endl;
		//keystore_file_o.flush();
		config0_file_o.close();

		//fwrite(password_hash, sizeof(unsigned char), password_hash_len, config0_file_o);
		//fclose(config0_file_o);
/* 
 * Do pliku .config idą dane etc
 * 
 */
		string keystore_path_set = "";
		string key_id_set = "";
		string password_set = "";
		printf("Keystore path: \n");
		c = (char) getchar();
		while ( c != 10 )
		{
			keystore_path_set.push_back(c);
			c = (char) getchar();
		}
		printf("Key ID: \n");
		c = (char) getchar();
		while ( c != 10 )
		{
			key_id_set.push_back(c);
			c = (char) getchar();
		}
		printf("Password to key: \n");
		set_stdin_echo(false);
		c = (char) getchar();
		while ( c != 10 )
		{
			password_set.push_back(c);
			c = (char) getchar();
		}
		set_stdin_echo(true);

		ofstream temp_o(".temp");
		keystore_path = keystore_path_set;
		key_id = key_id_set;
		password = password_set;
		temp_o << keystore_path_set << endl << key_id_set << endl << password_set << endl;
		temp_o.close();

		FILE *config0_i = fopen(".temp", "rb");
		FILE *config0_o = fopen(".config", "wb");

		crypt_file(key0, "encode", "CBC", config0_i, config0_o);

		fclose(config0_i);
		fclose(config0_o);
		remove(".temp");
	}
/*
 * Plik .config już istnieje
 */
	else
	{
		string password2 = "";
		printf("PIN:\n");
		set_stdin_echo(false);
		c = (char) getchar();
		while ( c != 10 )
		{
			password2.push_back(c);
			c = (char) getchar();
		}
		set_stdin_echo(true);
		//config_file_i0.close();
		//cout << "\nWpisales haslo: " << password2 << "\n";
		//FILE *config_file_i = fopen(".config", "rb");

		string line = "";
		getline(config_file_i0, line);

		//unsigned char buf[128];
		//int r = fread( buf, sizeof(unsigned char), 128, config_file_i);

		const char *password2_const_char = password2.c_str();
		unsigned char password2_uchar[password2.length()];
		for (int i = 0; i < password2.length(); i++)
		{
			password2_uchar[i] = password2_const_char[i];
		}	
		unsigned char password2_hash[EVP_MAX_MD_SIZE];
		unsigned int password2_hash_len = hash_sha512(password2_uchar, password2_hash, strlen(password2_const_char));
		string password2_hex = hex_table(password2_hash, password2_hash_len);
		//cout << "hash: " << password2_hex << endl;
		//cout << "line: " << line << endl;
		if ( password2_hex.compare(line.substr(0, 128)) == 0 )
		{
			printf("ACCESS GRANTED\n");
		}
		else
		{
			printf("ACCES DENIED\n");
			exit(-1);
		}

		FILE *config0_i = fopen(".config", "rb");
		FILE *config0_o = fopen(".temp", "wb");

		crypt_file(key0, "decode", "CBC", config0_i, config0_o);

		fclose(config0_i);
		fclose(config0_o);
		ifstream temp_i(".temp");
		keystore_path = "";
		key_id = "";
		password = "";
		getline(temp_i, keystore_path);	
		getline(temp_i, key_id);
		getline(temp_i, password);
		temp_i.close();
		//cout << keystore_path << key_id << password << endl;

		remove(".temp");
	}

	// znane są już ścieżki do keystore'a, key_id, password do keystore'a.
	
	unsigned char returned_key[128];
	int returned_key_len = get_decrypted_key_with_password("aleks", key_id, keystore_path, returned_key, password);
	unsigned char key[returned_key_len];
	for (int i = 0; i < returned_key_len; i++)
	{
		key[i] = returned_key[i];
	}
	FILE *music_file_f = fopen(argv[1], "rb");
	FILE *music_file_temp = fopen(".temp", "wb");
	if (returned_key_len != -1)
	{
		crypt_file(key, "decode", "CTR", music_file_f, music_file_temp);
	}
	else
	{
		exit(-1);
	}
// ---------------------ODTWARZANIE
//inicjalizacja
	if( SDL_Init(SDL_INIT_AUDIO) < 0 ) exit(1);
	int flags = MIX_INIT_OGG | MIX_INIT_FLAC;
	int a = Mix_Init(flags);
	if(a&flags != flags) 
	{
		printf("Mix_Init: %s\n", Mix_GetError());
	}

	if ( Mix_OpenAudio( MIX_DEFAULT_FREQUENCY, MIX_DEFAULT_FORMAT, 2, 4096) == -1 )
	{
	}
	//ładowanie pliku
	music = Mix_LoadMUS(".temp");
	if ( music == NULL )
	{
		printf("Nie zaladwoalem pliku %s\n", Mix_GetError());
	}
	else
	{
		printf("Playing...");
	}
// Puszczanie muzyki:
	if ( Mix_PlayMusic( music, 1 ) == -1 )
	{
	}
	while ( Mix_PlayingMusic() == 1 )
	{
		// Do something
	}
// Zatrzymanie:
	Mix_HaltMusic();
	clean();
	fclose(music_file_f);
	fclose(music_file_temp);
	remove(".temp");











	exit(0);
}
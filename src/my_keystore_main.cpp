/*
 * Autor: Aleksander Spyra
 * 
 * Własna implementacja keystore'a.
 * Na chwilę obecną możliwe jedynie dodawanie (generowanych przez program) losowych 32-bajtowych kluczy.
 * Do klucza przypisany jest identyfikator użytkownika oraz identyfikator hasła.
 * Plik wynikowy (keystore), o nazwie keystore_file, ma następującą postać (wszystko w jednej linii):
 * 128 znaków sha256(user_id + key_id) zapisanych heksadecymalnie, 128 znaków sha256(password) zapisanych heksadecymalnie, 96 znaków AES_256_CBC(key) zapisanych heksadecymalnie.
 * 
 * Kompilacja:
 * g++ -std=c++11 my_keystore_main.cpp -lcrypto
 *
 * Uruchomienie:
 * ./a.out add aleks klucz
 */

#include <cstdio>
#include <cstdlib> //exit, atof
#include <string>
#include "fun.h"
#include "my_crypto.h"
#include "my_keystore_f.h"

using namespace std;


/**
argv[1] - operacja do wykonania
argv[2] - id_użytkownika
argv[3] - id_klucza
*/
int main(int argc, const char* argv[])
{
	string operation(argv[1]);
	if (operation.compare("add") == 0)
	{
		printf("Adding new key to the keystore\n");
		string user_id(argv[2]);
		string key_id(argv[3]);
		string password = "";
		printf("Set password:\n");
		set_stdin_echo(false);
		char c = (char) getchar();
		while ( c != 10 )
		{
			password.push_back(c);
			c = (char) getchar();
		}
		set_stdin_echo(true);
		//cout << "\nWpisales haslo: " << password << "\n";
		
		ifstream keystore_file_i0("keystore_file");
		if (keystore_file_i0.is_open() == false)
		{
			keystore_file_i0.close();
			printf("Creating file keystore_file...\n");
			ofstream keystore_file_o0("keystore_file");
			keystore_file_o0.close();
		}
		ifstream keystore_file_i("keystore_file");
		int nr_of_lines = 0;
		string temp = "";
		while ( getline(keystore_file_i, temp) )
		{
			nr_of_lines += 1;
		}
		//printf("Liczba linii: %d\n", nr_of_lines);
// Generowanie klucza 0
		keystore_file_i.close();


		unsigned char* key;
		key = (unsigned char*)malloc(32*sizeof(unsigned char));
		random_device rd;
		mt19937 mersenne0(rd());
		for (int i = 0; i < 32; i++) 
		{
			key[i] = (unsigned char)mersenne0();
		}
// Generowanie klucza 1 i IV 1 do zaszyfrowania klucza 0
		ostringstream nr_of_lines_ss;
		nr_of_lines_ss << nr_of_lines;
		string nr_of_lines_string = nr_of_lines_ss.str();

		unsigned char* key0;
		key0 = (unsigned char*)malloc(32*sizeof(unsigned char));
		string pass_nr = password + nr_of_lines_string;
		seed_seq seed1 (pass_nr.begin(),pass_nr.end());
		mt19937 mersenne1(seed1);
		for (int i = 0; i < 32; i++) 
		{
			key0[i] = (unsigned char)mersenne1();
		}

		unsigned char* iv0;
		iv0 = (unsigned char*)malloc(16*sizeof(unsigned char));
		string nr_pass = nr_of_lines_string + password;
		seed_seq seed2 (pass_nr.begin(),pass_nr.end());
		mt19937 mersenne2(seed2);
		for (int i = 0; i < 16; i++)
		{
			iv0[i] = (unsigned char)mersenne2();
		}
//--------------------
//		Tak jak każe biblioteka
//---------------------
		unsigned char ciphertext[128];
		unsigned char decryptedtext[128];

		int decryptedtext_len, ciphertext_len;
		ERR_load_crypto_strings();
		OpenSSL_add_all_algorithms();
		OPENSSL_config(NULL);
//					do_zaszyfrowania dl_do_zaszyfrowania
		ciphertext_len = encrypt_aes_256_cbc( key, 32, key0, iv0, ciphertext);
		//printf("Dlugosc zaszyfrowanego klucza: %d\n", ciphertext_len);

		//decryptedtext_len = decrypt_aes_256_cbc( ciphertext, ciphertext_len, key0, iv0, decryptedtext);
		//decryptedtext[decryptedtext_len] = '\0';
/*		for (int i = 0; i < 32; i++) 
		{
			cout << (unsigned int)decryptedtext[i] << " ";
		}
		cout << endl;*/

		EVP_cleanup();
		ERR_free_strings();
// Hashowanie user_id+key_id
		string user_id_key_id_s = user_id + key_id;
		const char *user_id_key_id_const_char = user_id_key_id_s.c_str();
		unsigned char user_id_key_id_uchar[user_id_key_id_s.length()];
		for (int i = 0; i < user_id_key_id_s.length(); i++)
		{
			user_id_key_id_uchar[i] = user_id_key_id_const_char[i];
		}	
		unsigned char user_id_key_id_hash[EVP_MAX_MD_SIZE];

		unsigned int user_id_key_id_hash_len = hash_sha512(user_id_key_id_uchar, user_id_key_id_hash, strlen(user_id_key_id_const_char));

// Hashowanie password
		const char *password_const_char = password.c_str();
		unsigned char password_uchar[password.length()];
		for (int i = 0; i < password.length(); i++)
		{
			password_uchar[i] = password_const_char[i];
		}	
		unsigned char password_hash[EVP_MAX_MD_SIZE];

		unsigned int password_hash_len = hash_sha512(password_uchar, password_hash, strlen(password_const_char));

// Przygotowanie do wysyłki
		string cipher_key = hex_table(ciphertext, ciphertext_len);

		string hashed_ids = hex_table(user_id_key_id_hash, user_id_key_id_hash_len);
//		cout << "Dlugosc tablicy hex: " << hashed_ids.length() << endl;
		string hashed_password = hex_table(password_hash, password_hash_len);

		ofstream keystore_file_o("keystore_file", ios::app);
		keystore_file_o << hashed_ids << hashed_password << cipher_key << endl;
		//keystore_file_o.flush();
		keystore_file_o.close();


		free(key0); free(iv0);
	}

	exit(0);
}

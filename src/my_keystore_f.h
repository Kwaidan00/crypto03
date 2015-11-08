/*
 * Autor: Aleksander Spyra
 * 
 * Zbiór funkcji zajmujących się obsługą keystore'a.
 * Najistotniejsza jest metoda get_decrypted_key, która zwraca zdeszyfrowany klucz przechowywany w keystore.
 */

#include <termios.h>
#include <unistd.h>
#include <cstring>
#include <iomanip>
#include <iostream>
#include <fstream>
#include <sstream>
#include <random>

using namespace std;

/* Funkcja włączająca / wyłączająca wypisywanie ECHO na ekran.
 * Źródło:
 * http://stackoverflow.com/questions/1413445/read-a-password-from-stdcin */
void set_stdin_echo(bool enable)
{
    struct termios tty;
    tcgetattr(STDIN_FILENO, &tty);
    if( !enable )
        tty.c_lflag &= ~ECHO;
    else
        tty.c_lflag |= ECHO;

    (void) tcsetattr(STDIN_FILENO, TCSANOW, &tty);
}

string hex_table(unsigned char *table, int length)
{
	char buf[2];
	string hex = "";
	int c;
	for (int i = 0; i < length; i++)
	{
		c = int2charp2( (int)table[i], buf, 16);
		if (c == 1)
		{
			buf[1] = buf[0];
			buf[0] = '0';
		}
		hex.push_back(buf[0]);
		hex.push_back(buf[1]);
	}
	return hex;
}

string unhex_table(string s)
{
	char buf[2];
	string unhex = "";
	int i = 0;
	while (i < s.length())
	{
		buf[0] = s[i];
		buf[1] = s[i+1];
		i += 2;
		unhex.push_back((unsigned char)str2int(buf, 2, 16));
	}
	return unhex;
}

int get_decrypted_key(string user_id, string key_id, string keystore, unsigned char* decryptedtext)
{
	string user_id_key_id_s = user_id + key_id;
	const char *user_id_key_id_const_char = user_id_key_id_s.c_str();
	unsigned char user_id_key_id_uchar[user_id_key_id_s.length()];
	for (int i = 0; i < user_id_key_id_s.length(); i++)
	{
		user_id_key_id_uchar[i] = user_id_key_id_const_char[i];
	}	
	unsigned char user_id_key_id_hash[EVP_MAX_MD_SIZE];
	unsigned int user_id_key_id_hash_len = hash_sha512(user_id_key_id_uchar, user_id_key_id_hash, strlen(user_id_key_id_const_char));

	string hashed_ids = hex_table(user_id_key_id_hash, user_id_key_id_hash_len);
	
	ifstream keystore_file(keystore);
	if (keystore_file.is_open() == false)
	{
		printf("keystore file not found...\n");
	}
	int nr_of_line = 0;
	string line = "";
	while ( getline(keystore_file, line) )
	{
		if (hashed_ids.compare(line.substr(0, 128)) == 0)
		{
			//Odnaleziono zgodne hashe

			// Wczytanie hasla
			string password = "";
			cout << "Password for the user: " << user_id << "; key_id: " << key_id << endl;
			set_stdin_echo(false);
			char c = (char) getchar();
			while ( c != 10 )
			{
				password.push_back(c);
				c = (char) getchar();
			}
			set_stdin_echo(true);
			//cout << "\nWpisales haslo: " << password << "\n";
		// Hashowanie password
			const char *password_const_char = password.c_str();
			unsigned char password_uchar[password.length()];
			for (int i = 0; i < password.length(); i++)
			{
				password_uchar[i] = password_const_char[i];
			}	
			unsigned char password_hash[EVP_MAX_MD_SIZE];
			unsigned int password_hash_len = hash_sha512(password_uchar, password_hash, strlen(password_const_char));
			string password_hex = hex_table(password_hash, password_hash_len);
			if ( password_hex.compare(line.substr(128, 128)) == 0 )
			{
				printf("ACCESS GRANTED\n");
			}
			else
			{
				printf("ACCES DENIED\n");
				return -1;
			}
		// Wyciagnij ostatnie rzeczy z linii, odhexuj je - jest zaszyfrowany klucz. Utworzenie klucza i VI i odszyfrowanie.
			string encrypted_key = unhex_table( line.substr(256, 96) );
			unsigned char encrypted_key_u[encrypted_key.length()];
			for (int i = 0; i < encrypted_key.length(); i++)
			{
				encrypted_key_u[i] = encrypted_key[i];
			}
		//---------------------------------------------
			ostringstream nr_of_lines_ss;
			nr_of_lines_ss << nr_of_line;
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

			int decryptedtext_len;
			ERR_load_crypto_strings();
			OpenSSL_add_all_algorithms();
			OPENSSL_config(NULL);

			decryptedtext_len = decrypt_aes_256_cbc( encrypted_key_u, encrypted_key.length(), key0, iv0, decryptedtext);


			EVP_cleanup();
			ERR_free_strings();
	
			keystore_file.close();
			return decryptedtext_len;
		}
		nr_of_line += 1;
	}
	keystore_file.close();
	return -1;
}

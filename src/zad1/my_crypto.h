#include <random>
#include <openssl/aes.h>
#include <openssl/conf.h>
#include <openssl/evp.h>
#include <openssl/err.h>

using namespace std;

int encrypt_aes_256_cbc(unsigned char *text, int text_len, unsigned char *key, unsigned char *iv, unsigned char *ciphertext)
{
	EVP_CIPHER_CTX *ctx;
	int len;
	int ciphertext_len;
	if ( !(ctx = EVP_CIPHER_CTX_new()) )
	{
		//cout << "Error" << endl;
	}
	if ( EVP_EncryptInit_ex(ctx, EVP_aes_256_cbc(), NULL, key, iv) != 1)
	{
		//cout << "Error" << endl;
	}
	if ( EVP_EncryptUpdate(ctx, ciphertext, &len, text, text_len) != 1 )
	{
		//cout << "Error" << endl;
	}
	ciphertext_len = len;
	if ( EVP_EncryptFinal_ex(ctx, ciphertext + len, &len) != 1 )
	{
		//cout << "Error" << endl;
	}
	ciphertext_len += len;
	EVP_CIPHER_CTX_free(ctx);
	return ciphertext_len;
}

int decrypt_aes_256_cbc(unsigned char *ciphertext, int ciphertext_len, unsigned char *key, unsigned char *iv, unsigned char *plaintext)
{
	EVP_CIPHER_CTX *ctx;
	int len;
	int plaintext_len;
	if ( !(ctx = EVP_CIPHER_CTX_new()) )
	{
	}
	if ( EVP_DecryptInit_ex(ctx, EVP_aes_256_cbc(), NULL, key, iv) != 1 )
	{
	}
	if ( EVP_DecryptUpdate(ctx, plaintext, &len, ciphertext, ciphertext_len) != 1 )
	{
	}
	plaintext_len = len;
	if ( EVP_DecryptFinal_ex(ctx, plaintext + len, &len) != 1 )
	{
	}
	plaintext_len += len;
	EVP_CIPHER_CTX_free(ctx);
	return plaintext_len;
}


unsigned int hash_sha512(unsigned char *input, unsigned char *output, size_t input_len)
{
	EVP_MD_CTX *mdctx;
	const EVP_MD *md;

	unsigned int output_len;
	OpenSSL_add_all_digests();

	md = EVP_get_digestbyname("SHA512");
	if (!md) {}
	mdctx = EVP_MD_CTX_create();
	EVP_DigestInit_ex(mdctx, md, NULL);
	EVP_DigestUpdate(mdctx, input, input_len );
	EVP_DigestFinal_ex(mdctx, output, &output_len);
	EVP_MD_CTX_destroy(mdctx);
	EVP_cleanup();
	return output_len;
}

int decrypt_aes_256_file(unsigned char *key, unsigned char *iv, FILE *input, FILE *output, string mode)
{
	int bufsize = 4096;
	unsigned char buf[bufsize];
	unsigned char decrypt_buf[bufsize*2];
	EVP_CIPHER_CTX *ctx;
	int len, r;
	int plaintext_len;
	if ( !(ctx = EVP_CIPHER_CTX_new()) )
	{
	}
	if ( mode.compare("CTR") == 0 )
	{
		if ( EVP_DecryptInit_ex(ctx, EVP_aes_256_ctr(), NULL, key, iv) != 1 )
		{
		}
	}
	else if ( mode.compare("CBC") == 0 )
	{
		if ( EVP_DecryptInit_ex(ctx, EVP_aes_256_cbc(), NULL, key, iv) != 1 )
		{
		}
	}
	else if ( mode.compare("GCM") == 0 )
	{
		unsigned char iv0[12];
		for (int i = 0; i < 12; i++)
		{
			iv0[i] = iv[i];
		}
		if ( EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, key, iv0) != 1 )
		{
		}		
	}

	do
	{
		r = fread( buf, sizeof(unsigned char), bufsize, input );
		if ( EVP_DecryptUpdate(ctx, decrypt_buf, &len, buf, r) != 1 )
		{
			//cout << "Error" << endl;
		}
		fwrite(decrypt_buf, sizeof(unsigned char), len, output);
	}
	while (r == bufsize);

	if ( EVP_DecryptFinal_ex(ctx, decrypt_buf, &len) != 1 )
	{
		//cout << "Error" << endl;
	}
	fwrite(decrypt_buf, sizeof(unsigned char), len, output);

	plaintext_len += len;
	EVP_CIPHER_CTX_free(ctx);
	return plaintext_len;
}

int encrypt_aes_256_file(unsigned char *key, unsigned char *iv, FILE *input, FILE *output, string mode)
{
	int bufsize = 4096;
	unsigned char buf[bufsize];
	unsigned char cipher_buf[bufsize*2];
	EVP_CIPHER_CTX *ctx;
	int len, r;
	int ciphertext_len;
	if ( !(ctx = EVP_CIPHER_CTX_new()) )
	{
		//cout << "Error" << endl;
	}
	if ( mode.compare("CTR") == 0 )
	{
		if ( EVP_EncryptInit_ex(ctx, EVP_aes_256_ctr(), NULL, key, iv) != 1)
		{
			//cout << "Error" << endl;
		}
	}
	else if ( mode.compare("CBC") == 0 )
	{
		if ( EVP_EncryptInit_ex(ctx, EVP_aes_256_cbc(), NULL, key, iv) != 1)
		{
			//cout << "Error" << endl;
		}
	}
	else if ( mode.compare("GCM") == 0 )
	{
		unsigned char iv0[12];
		for (int i = 0; i < 12; i++)
		{
			iv0[i] = iv[i];
		}
		if ( EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, key, iv0) != 1)
		{
		}
	}


	do
	{
		r = fread( buf, sizeof(unsigned char), bufsize, input );
		if ( EVP_EncryptUpdate(ctx, cipher_buf, &len, buf, r) != 1 )
		{
			//cout << "Error" << endl;
		}
		fwrite(cipher_buf, sizeof(unsigned char), len, output);
	}
	while (r == bufsize);
	
	if ( mode.compare("GCM") == 0 )
	{
		if ( EVP_EncryptUpdate(ctx, cipher_buf, &len, buf, r) != 1 )
		{
			//cout << "Error" << endl;
		}
	}
	ciphertext_len = len;
	if ( EVP_EncryptFinal_ex(ctx, cipher_buf, &len) != 1 )
	{
		//cout << "Error" << endl;
	}
	fwrite(cipher_buf, sizeof(unsigned char), len, output);
	ciphertext_len += len;
	EVP_CIPHER_CTX_free(ctx);
	return ciphertext_len;
}

void crypt_file(unsigned char *key, string mode, string encoding_mode, FILE *input, FILE *output)
	{
		unsigned char* iv;
		iv = (unsigned char*)malloc(16*sizeof(unsigned char));
	
		if ( mode.compare("encode") == 0 )
		{
			random_device rd;
			mt19937 mersenne0(rd());
			for (int i = 0; i < 16; i++) 
			{
				iv[i] = (unsigned char)mersenne0();
			}
			fwrite(iv, sizeof(unsigned char), 16, output);
		}
		else if ( mode.compare("decode") == 0 )
		{
			int r = fread( iv, sizeof(unsigned char), 16, input );
		}

	
		if ( encoding_mode.compare("CBC") == 0 || encoding_mode.compare("CTR") == 0 || encoding_mode.compare("GCM") == 0 )
		{
			if ( mode.compare("encode") == 0 )
			{
				encrypt_aes_256_file(key, iv, input, output, encoding_mode);
			}
			else if ( mode.compare("decode") == 0 )
			{
				decrypt_aes_256_file(key, iv, input, output, encoding_mode);
			}
		}
	}

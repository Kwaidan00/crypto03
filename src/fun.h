#define FUN_BUFSIZE 128

int int2charp2(int integ, char* buf, int base)
{
	//printf("\n\t\t----int2charp----\n");
	int i, counter, result, temp_int;
	int temp[FUN_BUFSIZE];
	counter = 0;
	while (integ != 0)
	{
		temp[counter] = integ % base;
		integ = integ / base;
		counter++;
	}
	if (base == 10)
	{
		for (i = 0; i < counter; i++)
		{
			buf[i] = (char)(temp[counter - i - 1] + 48);
		}
	}
	else if (base == 16)
	{
		for (i = 0; i < counter; i++)
		{
			temp_int = temp[counter - i - 1];
			if (temp_int < 10)
			{
				buf[i] = (char)(temp_int + 48);
			}
			else
			{
				buf[i] = (char)(temp_int + 55);
			}
		}
	}
	else if (base == 2)
	{
		//if (counter < length)
		//{
		//	for (i = counter; i < length; i++)
		//	{
		//		temp[i] = 0;
		//	}
		//}
		//printf("\t\tcounter: %d\n", counter);
		//for (i = 0; i < 16; i++)
		//{
		//	printf("\t\ttemp[%d]: %d\n", i, temp[i]);
		//}
		for (i = 0; i < counter; i++)
		{
			if (temp[counter - i - 1] == 0)
			{
				buf[i] = '0';
			}
			else
			{
				buf[i] = '1';
			}
		}
	}
	//printf("-----\nHey, tu int2charp. Zawartosci bufora:\n%s\n---END OF int2charp\n", buf);
	//if (base != 2)
		return counter;
	//else
	//	return length;
}

int int2charp(int integ, char* buf, int base, int length)
{
	//printf("\n\t\t----int2charp----\n");
	int i, counter, *temp, result, temp_int;
	temp = (int*)malloc(FUN_BUFSIZE*sizeof(int));
	counter = 0;
	while (integ != 0)
	{
		temp[counter] = integ % base;
		integ = integ / base;
		counter++;
	}
	if (base == 10)
	{
		for (i = 0; i < counter; i++)
		{
			buf[i] = (char)(temp[counter - i - 1] + 48);
		}
	}
	else if (base == 16)
	{
		for (i = 0; i < counter; i++)
		{
			temp_int = temp[counter - i - 1];
			if (temp_int < 10)
			{
				buf[i] = (char)(temp_int + 48);
			}
			else
			{
				buf[i] = (char)(temp_int + 55);
			}
		}
	}
	else if (base == 2)
	{
		if (counter < length)
		{
			for (i = counter; i < length; i++)
			{
				temp[i] = 0;
			}
		}
		//printf("\t\tcounter: %d\n", counter);
		//for (i = 0; i < 16; i++)
		//{
		//	printf("\t\ttemp[%d]: %d\n", i, temp[i]);
		//}
		for (i = 0; i < length; i++)
		{
			if (temp[length - i - 1] == 0)
			{
				buf[i] = '0';
			}
			else
			{
				buf[i] = '1';
			}
		}
	}
	free(temp);
	//printf("-----\nHey, tu int2charp. Zawartosci bufora:\n%s\n---END OF int2charp\n", buf);
	if (base != 2)
		return counter;
	else
		return length;
}

int str2int(char *buf, int buf_c, int base)
{
	//printf("\n------STR2INT-----\nbuf_c: %d", buf_c);
	int i, mnoznik, result;
	char c;
	mnoznik = 1;
	result = 0;
	for (i = buf_c-1; i >= 0; i--)
	{
		c = buf[i];
		if (base == 10)
		{
			if ( (int)c >= 48 && (int)c <= 57) //jest cyfrą 0-9
			{
				result = result + ((int)c - 48)*mnoznik;
				mnoznik = mnoznik * 10;
			}
			else
			{
				return 0;
			}
		}
		if (base == 16)
		{
			if ( (int)c >= 48 && (int)c <= 57 ) 
			{
				result = result + ((int)c - 48)*mnoznik;
			}
			else if ( (int)c >= 65 && (int)c <= 70 ) 
			{
				result = result + ((int)c - 55)*mnoznik;
			}
			else if ( (int)c >= 97 && (int)c <= 102 )
			{
				result = result + ((int)c - 87)*mnoznik;
			}
			else
			{
				return 0;
			}
			mnoznik = mnoznik * 16;
		}
		if (base == 2)
		{
			if ( c == '1')
			{
				result = result + 1 * mnoznik;
			}
			else if (c == '0')
			{
			}
			else
			{
				return 0;
			}
			mnoznik = mnoznik * 2;
		}
	}
	return result;
}

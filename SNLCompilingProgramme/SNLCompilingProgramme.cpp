// SNLCompilingProgramme.cpp : 此文件包含 "main" 函数。程序执行将在此处开始并结束。
//

#include "pch.h"

#include <stdio.h>

#include <string.h>

#include <stdlib.h>

#include <ctype.h>



#define KEYWORD_NUM 9



//二元组, 用于储存读入的单词

typedef struct

{

	int code;				//单词识别码

	char value[20];			//单词自身值

} Tuple;



//关键字

const char keyword[KEYWORD_NUM][20] = { "if", "else", "int", "for", "while", "do", "return", "break", "continue" };



//判断读入的字符串是否为字符串

bool is_keyword(char* str);

//读入一个字符串

void read_string();

//读入一个数字

void read_digit();

//读入一个运算符

void read_operator();

//读入一个界符

void read_delimiter();



char c;				//指向下一个要读入的字符

FILE* fp;			//词法分析器要读入的字符串， 储存在一个文件中



Tuple tuples[100];		//储存产生的二元式

int num = 0;



int main()

{

	char filename[20];

	printf("Input Source Code Filename: ");

	scanf("%s", filename);

	fp = fopen(filename, "r");

	if (fp == NULL)

	{

		printf("Open File Error\n");

		exit(0);

	}



	while (!feof(fp))

	{

		c = fgetc(fp);

		//字符串

		if (isalpha(c))

		{

			read_string();

		}

		//数字

		else if (isdigit(c))

		{

			read_digit();

		}

		//运算符

		else if (c == '>' || c == '<' || c == '=' || c == '!'

			|| c == '+' || c == '-' || c == '*' || c == '/')

		{

			read_operator();

		}

		//界符

		else if (c == ',' || c == '(' || c == ')' || c == '}' || c == '{')

		{

			read_delimiter();

		}

	}



	for (int i = 0; i < num; i++)

	{

		printf("< %d, %s >\n", tuples[i].code, tuples[i].value);

	}

	fclose(fp);



	//把产生的中间二元式写入文件

	fp = fopen("middle_code.txt", "w");

	for (int i = 0; i < num; i++)

	{

		fprintf(fp, "<%d, %s>\n", tuples[i].code, tuples[i].value);

	}

	fclose(fp);



	return 0;

}



void read_delimiter()

{

	int cursor = 0;

	char buffer[20];



	buffer[cursor++] = c;

	buffer[cursor] = '\0';



	tuples[num].code = 5;

	strcpy((char*)tuples[num++].value, buffer);

}



void read_digit()

{

	int cursor = 0;

	char buffer[20];



	buffer[cursor++] = c;

	c = fgetc(fp);

	//如果是数字， 则继续读入

	while (isdigit(c))

	{

		buffer[cursor++] = c;

		c = fgetc(fp);

	}

	buffer[cursor] = '\0';

	//多读了一个字符， 向后退还一个字符



	fseek(fp, -1, SEEK_CUR);



	tuples[num].code = 3;

	strcpy((char*)tuples[num++].value, buffer);

}



void read_operator()

{

	int cursor = 0;

	char buffer[20];



	buffer[cursor++] = c;

	c = fgetc(fp);

	//判断运算符是否为>=, <= ...

	if (c == '=')

	{

		buffer[cursor++] = c;

	}

	else

	{

		//多读了一个字符， 向后退还一个字符

		fseek(fp, -1, SEEK_CUR);

	}

	buffer[cursor] = '\0';



	tuples[num].code = 4;

	strcpy((char*)tuples[num++].value, buffer);

}



void read_string()

{

	int cursor = 0;

	char buffer[20];



	buffer[cursor++] = c;

	c = fgetc(fp);

	//如果是字符或数字， 则继续读入

	while (isalpha(c) || isdigit(c))

	{

		buffer[cursor++] = c;

		c = fgetc(fp);

	}

	buffer[cursor] = '\0';



	//多读了一个字符， 向后退回一个字符

	fseek(fp, -1, SEEK_CUR);



	//判读是关键字， 还是标识符	

	if (is_keyword(buffer))

	{

		tuples[num].code = 1;

		strcpy((char*)tuples[num++].value, buffer);

	}

	else

	{

		tuples[num].code = 2;

		strcpy((char*)tuples[num++].value, buffer);

	}

}



bool is_keyword(char* str)

{

	for (int i = 0; i < KEYWORD_NUM; i++)

	{

		if (strcmp(keyword[i], str) == 0)

		{

			return true;

		}

	}



	return false;
}

// 运行程序: Ctrl + F5 或调试 >“开始执行(不调试)”菜单
// 调试程序: F5 或调试 >“开始调试”菜单

// 入门提示: 
//   1. 使用解决方案资源管理器窗口添加/管理文件
//   2. 使用团队资源管理器窗口连接到源代码管理
//   3. 使用输出窗口查看生成输出和其他消息
//   4. 使用错误列表窗口查看错误
//   5. 转到“项目”>“添加新项”以创建新的代码文件，或转到“项目”>“添加现有项”以将现有代码文件添加到项目
//   6. 将来，若要再次打开此项目，请转到“文件”>“打开”>“项目”并选择 .sln 文件

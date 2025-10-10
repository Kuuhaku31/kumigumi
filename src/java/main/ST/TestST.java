package ST;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 2025-10-10 Java 上机作业
 */
public
class TestST
{
    static
    void main(String[] args)
    {
        IO.println("TestST");
        IO.println("args: ");
        IO.println(Arrays.toString(args));

        ST5();
    }

    // 1. 打印信息
    static
    void ST1()
    {
        IO.println("Hello，Java");
        IO.println("Welcome to Java");
    }

    // 2. 冒泡算法对10个数进行排序
    static
    void ST2()
    {
        int[] src = {1, 2, 4, 2, 1, 22, 432, 11, 23, 4};

        IO.println("排序前: " + Arrays.toString(src));

        // 冒泡排序算法
        for(int i = 0; i < src.length - 1; i++)
        {
            for(int j = 0; j < src.length - 1 - i; j++)
            {
                if(src[j] > src[j + 1])
                {
                    // 交换
                    int temp = src[j];
                    src[j] = src[j + 1];
                    src[j + 1] = temp;
                }
            }
        }

        IO.println("排序后: " + Arrays.toString(src));
    }

    // 3. 编写程序判断101-200之间有多少个素数，并输出所有素数
    static
    void ST3()
    {
        int count = 0;
        ArrayList<Integer> primes = new ArrayList<>();

        for(int num = 101; num <= 200; num++)
        {
            boolean isPrime = true;

            // 判断 num 是否为素数
            for(int i = 2; i <= Math.sqrt(num); i++)
            {
                if(num % i == 0)
                {
                    isPrime = false;
                    break;
                }
            }

            if(isPrime)
            {
                count++;
                primes.add(num);
            }
        }

        IO.println("101-200之间的素数有 " + count + " 个");
        IO.println("素数列表: " + primes);
    }

    // 4. 序打印出四位数中所有的四位数各位上的数字的四次方之和等于本身的数
    static
    void ST4()
    {
        IO.println("四位数中所有的四位数各位上的数字的四次方之和等于本身的数有:");

        for(int num = 1000; num <= 9999; num++)
        {
            int sum = 0;
            int temp = num;

            while(temp != 0)
            {
                int digit = temp % 10;
                sum += (int) Math.pow(digit, 4);
                temp /= 10;
            }

            if(sum == num)
            {
                IO.println(num);
            }
        }
    }

    // 5. 编写程序利用不规则二维数组存储9*9乘法口诀的结果，并按照9*9乘法口诀的格式输出
    static
    void ST5()
    {
        int[][] multiplicationTable = new int[9][];
        for(int i = 0; i < 9; i++)
        {
            multiplicationTable[i] = new int[i + 1];
            for(int j = 0; j <= i; j++)
            {
                multiplicationTable[i][j] = (i + 1) * (j + 1);
            }
        }
        IO.println("9*9乘法口诀表:");
        for(int i = 0; i < multiplicationTable.length; i++)
        {
            for(int j = 0; j < multiplicationTable[i].length; j++)
            {
                IO.print((j + 1) + " * " + (i + 1) + " = " + multiplicationTable[i][j] + "\t");
            }
            IO.println();
        }
    }
}

# app.py
import time


def loop():
    while True:
        print("Looping...")
        time.sleep(1)


def main():
    print("Hello from Docker Python App!")
    print("Current time:", time.strftime("%Y-%m-%d %H:%M:%S"))


if __name__ == "__main__":
    main()
    # loop()

FROM python

WORKDIR /app
COPY requirements.txt ./

RUN apt-get update && apt-get install -y unixodbc
RUN pip install --no-cache-dir -r requirements.txt

# CMD [ "python", "src/main.py" ]

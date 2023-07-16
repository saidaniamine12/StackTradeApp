import os
import random
import subprocess
import datetime
import time

# Set up the range of months (June to July)
months = [6, 7]  # June and July
days_of_week = [0, 1, 2, 3, 4, 5]  # Monday to Saturday (0 = Monday, 5 = Saturday)

# Function to run a git commit command with a common message
def commit_with_random_date():
    # Generate random date between 2 PM and 6 PM
    hour = random.randint(14, 18)  # Change the range to 14 to 18
    minute = random.randint(0, 59)
    second = random.randint(0, 59)

    # Generate a random date (from June to July)
    month = random.choice(months)
    day = random.randint(1, 28)  # Avoiding overflow for months with fewer days
    year = 2023

    # Create a datetime object
    commit_time = datetime.datetime(year, month, day, hour, minute, second)
    commit_date = commit_time.strftime("%Y-%m-%dT%H:%M:%S")

    # Set the environment variables and commit with the random date
    os.environ["GIT_AUTHOR_DATE"] = commit_date
    os.environ["GIT_COMMITTER_DATE"] = commit_date

    # Commit with a common message (e.g., "Update files")
    commit_message = "search engine updates"

    # Execute the commit command
    subprocess.run(["git", "add", "."])  # Add all changes
    subprocess.run(["git", "commit", "--amend", "--no-edit", "--date", commit_date, "-m", commit_message])  # Amend commit with new date and message
    print(f"Committed with message '{commit_message}' on {commit_date}")

    # Push the commit immediately after each one
    subprocess.run(["git", "push", "--force"])
    print(f"Pushed commit with message '{commit_message}' on {commit_date}")

# Main function to run the script
def generate_commits():
    for month in months:
        for day in range(1, 29):  # Days 1-28 for simplicity
            if datetime.date(2024, month, day).weekday() in days_of_week:  # Monday to Saturday
                num_commits = random.randint(1, 3)  # Number of commits for the day (between 1 and 3)
                for _ in range(num_commits):
                    commit_with_random_date()
                    time.sleep(random.randint(5, 20))  # Random sleep to simulate real usage
                print(f"Finished commits for {month}/{day}/2024.")

if __name__ == "__main__":
    generate_commits()

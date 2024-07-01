#include <sys/inotify.h>
#include <limits.h>
#include <unistd.h>
#include <iostream>
#include <string.h>
#include <errno.h>
#include <cstdlib>
#include <csignal>

// Global variables to handle the inotify file descriptor
int inotifyFd = -1;

void signalHandler(int signum) {
    if (inotifyFd != -1) {
        close(inotifyFd); // Close the inotify file descriptor
        inotifyFd = -1;
    }
    std::cout << "Interrupt signal (" << signum << ") received.\n";
    exit(signum);
}

// Helper function to check if a file has a specific extension
bool hasExtension(const std::string& filename, const std::string& extension) {
    return filename.size() >= extension.size() &&
           filename.compare(filename.size() - extension.size(), extension.size(), extension) == 0;
}

void handleEvents(int fd, int wd) {
    char buf[4096] __attribute__ ((aligned(__alignof__(struct inotify_event))));
    const struct inotify_event *event;
    ssize_t len;
    char *ptr;

    while (true) {
        len = read(fd, buf, sizeof buf);
        if (len == -1 && errno != EAGAIN) {
            perror("read");
            exit(EXIT_FAILURE);
        }

        if (len <= 0)
            break;

        for (ptr = buf; ptr < buf + len;
             ptr += sizeof(struct inotify_event) + event->len) {

            event = (const struct inotify_event *) ptr;

            if (event->len > 0 && hasExtension(event->name, ".h264")) {
                if (event->mask & IN_CREATE) {
                    std::cout << "Event mask: " << event->mask << " IN_CREATE\n";
                    std::cout << "File name: " << event->name << std::endl;
                }

                if (event->mask & IN_MODIFY) {
                    std::cout << "Event mask: " << event->mask << " IN_MODIFY\n";
                    std::cout << "File name: " << event->name << std::endl;
                }

                if (event->mask & IN_CLOSE_WRITE) {
                    std::cout << "Event mask: " << event->mask << " IN_CLOSE_WRITE\n";
                    std::cout << "File name: " << event->name << std::endl;
                }

                if (event->mask & IN_OPEN) {
                    std::cout << "Event mask: " << event->mask << " IN_OPEN\n";
                    std::cout << "File name: " << event->name << std::endl;
                }

                if (event->mask & IN_ACCESS) {
                    std::cout << "Event mask: " << event->mask << " IN_ACCESS\n";
                    std::cout << "File name: " << event->name << std::endl;
                }

                if (event->mask & IN_CLOSE_NOWRITE) {
                    std::cout << "Event mask: " << event->mask << " IN_CLOSE_NOWRITE\n";
                    std::cout << "File name: " << event->name << std::endl;
                }

                if (event->mask & IN_ATTRIB) {
                    std::cout << "Event mask: " << event->mask << " IN_ATTRIB\n";
                    std::cout << "File name: " << event->name << std::endl;
                }
            }

            // Handle directory events
            if (event->mask & IN_ISDIR) {
                if (event->mask & IN_OPEN) {
                    std::cout << "Event mask: " << event->mask << " IN_OPEN IN_ISDIR\n";
                }

                if (event->mask & IN_ACCESS) {
                    std::cout << "Event mask: " << event->mask << " IN_ACCESS IN_ISDIR\n";
                }

                if (event->mask & IN_CLOSE_NOWRITE) {
                    std::cout << "Event mask: " << event->mask << " IN_CLOSE_NOWRITE IN_ISDIR\n";
                }
            }
        }
    }
}

int main(int argc, char *argv[]) {
    const std::string defaultDirectory = "."; // Your default directory here
    std::string directory = defaultDirectory;

    if (argc > 1) {
        directory = argv[1];
    }

    // Check if the directory exists
    if (access(directory.c_str(), F_OK) == -1) {
        std::cerr << "Directory does not exist: " << directory << std::endl;
        exit(EXIT_FAILURE);
    }

    // Register signal handler
    struct sigaction sa;
    sa.sa_handler = signalHandler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sigaction(SIGINT, &sa, NULL);

    inotifyFd = inotify_init1(IN_NONBLOCK);
    if (inotifyFd == -1) {
        perror("inotify_init1");
        exit(EXIT_FAILURE);
    }

    int wd = inotify_add_watch(inotifyFd, directory.c_str(), IN_CREATE | IN_MODIFY | IN_CLOSE_WRITE | IN_OPEN | IN_ACCESS | IN_CLOSE_NOWRITE | IN_ATTRIB);
    if (wd == -1) {
        perror("inotify_add_watch");
        close(inotifyFd);
        exit(EXIT_FAILURE);
    }

    std::cout << "Watching directory: " << directory << std::endl;

    while (true) {
        handleEvents(inotifyFd, wd);
        sleep(1);  // Thêm khoảng thời gian đợi
    }

    close(wd);
    close(inotifyFd);

    return 0;
}

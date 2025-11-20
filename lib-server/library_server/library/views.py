from django.shortcuts import render
from rest_framework import viewsets
from .models import Book, BorrowRecord
from .serializers import BookSerializer, BorrowRecordSerializer
from rest_framework.decorators import action
from rest_framework.response import Response
from django.utils import timezone

class BookViewSet(viewsets.ModelViewSet):
    queryset = Book.objects.all()
    serializer_class = BookSerializer


class BorrowRecordViewSet(viewsets.ModelViewSet):
    queryset = BorrowRecord.objects.all()
    serializer_class = BorrowRecordSerializer

    # 自定义借书接口
    @action(detail=True, methods=['POST'])
    def borrow(self, request, pk=None):
        record = self.get_object()
        if record.book.stock <= 0:
            return Response({"error": "no stock"}, status=400)

        record.book.stock -= 1
        record.book.save()
        return Response({"status": "borrowed"})

    # 自定义还书接口
    @action(detail=True, methods=['POST'])
    def return_book(self, request, pk=None):
        record = self.get_object()
        if record.returned:
            return Response({"error": "already returned"}, status=400)

        record.returned = True
        record.return_date = timezone.now()
        record.book.stock += 1
        record.book.save()
        record.save()
        return Response({"status": "returned"})